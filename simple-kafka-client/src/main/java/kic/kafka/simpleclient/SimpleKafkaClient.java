package kic.kafka.simpleclient;

import com.github.benmanes.caffeine.cache.LoadingCache;
import kic.kafka.simpleclient.cache.CacheFactory;
import kic.kafka.simpleclient.cache.CachedConsumer;
import kic.kafka.simpleclient.cache.ConsumerCacheKey;
import kic.kafka.simpleclient.cache.ProducerCacheKey;
import kic.kafka.simpleclient.stream.KafkaSimpleStream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.stream.Collectors.toList;

// This class should be a singleton per kafka cluster
public class SimpleKafkaClient {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleKafkaClient.class);
    private static final int MAX_RETRIES = 3;
    private final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(1);
    private final Properties kafkaProperties;
    private final LoadingCache<ProducerCacheKey, KafkaProducer> producers;
    private final LoadingCache<ConsumerCacheKey, CachedConsumer> consumers;
    private KafkaAdminClient adminClient = null;

    public SimpleKafkaClient(Properties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        this.producers = CacheFactory.newProducerCache(kafkaProperties);
        this.consumers = CacheFactory.newConsumerCache(kafkaProperties);
    }

    public <K, V>RecordMetadata send(String topic, K key, V value) {
        // Hide away the producer, we keep the producers depending on the key/value types in a caffeine cache
        ProducerCacheKey cacheKey = new ProducerCacheKey(key.getClass(), value.getClass());

        try {
            RecordMetadata rm = (RecordMetadata) producers.get(cacheKey)
                                                          .send(new ProducerRecord(topic, key, value))
                                                          .get();

            if (LOG.isDebugEnabled()) LOG.debug("pushed: {}:{} / {}", key, value, rm);
            return rm;
        } catch (InterruptedException | ExecutionException e) {
            throw new IOError(e);
        }
    }

    public <K, V>Records<K, V> poll(String name, String topic, Class<K> keyClass, Class<V> valueClass, long offset, long timeOutInMs) {
        // Hide away the kafkaConsumer, we keep the consumers depending on a name + a topic in a caffeine cache
        ConsumerCacheKey cacheKey = new ConsumerCacheKey(name, topic, keyClass, valueClass);
        long rebalanceTimeout = 10000L;

        // only one poll at a time for the same kafkaConsumer is allowed
        synchronized (cacheKey.getLockObject()) {
            for (int retry = 0; retry < MAX_RETRIES; retry++) {
                try {
                    CachedConsumer cachedConsumer = consumers.get(cacheKey);
                    KafkaConsumer<K, V> consumer = (KafkaConsumer<K, V>) cachedConsumer.kafkaConsumer;

                    // we need an async background task because if a coumer is in the state of rebalancing
                    // I have experienced some long waiting operation (longer then the poll timeout!)
                    if (offset != cachedConsumer.lastPulledOffset + 1 || offset < 0) seek(consumer, topic, offset);
                    ConsumerRecords<K, V> consumedRecords = consumer.poll(timeOutInMs);
                    /* TODO we can enable the following via configuration
                    ConsumerRecords<K, V> consumedRecords = timeOutableOperation(() -> {
                        // we should only need to seek if the current offset is different
                        if (cachedConsumer.lastPulledOffset < offset - 1) seek(consumer, topic, offset);
                        return consumer.poll(timeOutInMs);
                    }, rebalanceTimeout);*/


                    Records<K, V> recordsAndOffset = mapRecords(consumedRecords);

                    // and we remember the last offset
                    if (!recordsAndOffset.isEmpty()) consumers.put(cacheKey, cachedConsumer.withOffset(recordsAndOffset.lastOffset));

                    return recordsAndOffset;
                } catch (Exception e) {
                    LOG.warn("poll failed - retry {}/{}\n{}", retry + 1, MAX_RETRIES, e);
                    consumers.invalidate(cacheKey);
                }
            }
        }

        // if everyting fails return and emtpy list
        return new Records<>(new ArrayList<>(), -1, -1);
    }

    public void seek(KafkaConsumer<?, ?> consumer, String topic, long offset) {
        LOG.debug("'{}' seek {}", topic, offset);
        List<TopicPartition> topicPartitions = Arrays.asList(new TopicPartition(topic, 0));

        if (offset >= 0) {
            // we only support one partition at the moment
            for (TopicPartition topicPartition : topicPartitions)
                consumer.seek(topicPartition, offset);
        } else {
            consumer.seekToEnd(topicPartitions);
        }
    }

    // FIXME we should add the same logic with passing classes like in the comsumer: Class<K> keyClass, Class<V> valueClass
    public KafkaSimpleStream streaming(String streamId) {
        return new KafkaSimpleStream(new PropertiesExtender(kafkaProperties).with(StreamsConfig.APPLICATION_ID_CONFIG, streamId)
                                                                            .with(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                                                                            .extend());
    }

    public void deleteAllTopics() {
        Set<String> topics = listTopics();
        LOG.info("trying to delete topics: {}", topics);
        deleteTopic(topics.toArray(new String[0]));
    }

    /**
     * We do not know if this is thread safe. threat it as singleton but synchronize access to this object
     * @return kafka admin client
     */
    public synchronized KafkaAdminClient getAdminClient() {
        if (adminClient == null) adminClient = createClient(0);
        return adminClient;
    }

    private KafkaAdminClient createClient(int tried) {
        try {
            return (KafkaAdminClient) AdminClient.create(kafkaProperties);
        } catch (KafkaException e) {
            if (tried < Integer.parseInt(kafkaProperties.getProperty("max.connection.retry", "10"))) {
                try {
                    Thread.sleep(1000L);
                    return createClient(tried++);
                } catch (InterruptedException e1) {
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }

    public Set<String> listTopics() {
        try {
            return getAdminClient().listTopics()
                                   .names()
                                   .get();
        } catch (InterruptedException | ExecutionException e) {
            // FIXME what should we do? e.printStackTrace();
            LOG.error("", e);
            return new HashSet<>();
        }
    }

    public void createTopic(String... topic) {
        createTopic(1, 1, topic);
    }

    // make this private as we currently only support one partition
    private void createTopic(int nrOfPartitions, int replicas, String... topics) {
        List<NewTopic> newTopics = Arrays.stream(topics)
                                         .map(topic -> new NewTopic(topic, nrOfPartitions, (short) replicas))
                                         .collect(toList());

        try {
            getAdminClient().createTopics(newTopics)
                            .all()
                            .get();
        } catch (TopicExistsException existsException) {
            LOG.warn("Topic already exists");
        } catch (InterruptedException | ExecutionException e) {
            // FIXME what should we do? e.printStackTrace();
            LOG.error("", e);
        }
    }

    public void deleteTopic(String... topics) {
        try {
            getAdminClient().deleteTopics(Arrays.asList(topics))
                            .all()
                            .get();

            List<String> deleteTopics = new ArrayList<>(Arrays.asList(topics));
            while (deleteTopics.size() > 0) {
                deleteTopics.retainAll(listTopics());
                Thread.sleep(200L);
            }
        } catch (InterruptedException | ExecutionException e) {
            // FIXME what should we do? e.printStackTrace();
        }
    }

    private <K, V>Records<K, V> mapRecords(ConsumerRecords<K, V> consumerRecords) {
        List<ConsumerRecord<K, V>> records = new ArrayList<>(consumerRecords.count());
        Iterator<ConsumerRecord<K, V>> it = consumerRecords.iterator();

        while (it.hasNext()) records.add(it.next());
        long startOffset = records.size() > 0 ? records.get(0).offset() : -1;
        long endOffset = records.size() > 0 ? records.get(records.size() - 1).offset() : -1;

        if (LOG.isDebugEnabled()) LOG.debug("polled rows at offsets {} - {} : {}, {}", startOffset, endOffset, records.size(), records);
        return new Records<>(records, startOffset, endOffset);
    }

    private <T>T timeOutableOperation(Callable<T> task, long timeoutInMs) throws InterruptedException, ExecutionException, TimeoutException {
        return backgroundExecutor.submit(task).get(timeoutInMs, TimeUnit.MILLISECONDS);
    }
}
