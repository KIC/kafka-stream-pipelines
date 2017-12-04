package kic.kafka.simpleclient;

import com.github.benmanes.caffeine.cache.LoadingCache;
import kic.kafka.simpleclient.cache.CacheFactory;
import kic.kafka.simpleclient.cache.CachedConsumer;
import kic.kafka.simpleclient.cache.ConsumerCacheKey;
import kic.kafka.simpleclient.cache.ProducerCacheKey;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import static java.util.stream.Collectors.toList;

// This class should be a singleton per kafka cluster
public class SimpleKafkaClient {
    private static final int MAX_RETRIES = 3;
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
            return (RecordMetadata) producers.get(cacheKey)
                                             .send(new ProducerRecord(topic, key, value))
                                             .get();
        } catch (InterruptedException | ExecutionException e) {
            // FIXME what should we do? e.printStackTrace();
            return null;
        }
    }

    public <K, V>List<ConsumerRecord<K, V>> poll(String name, String topic, Class<K> keyClass, Class<V> valueClass, long offset, long timeOutInMs) {
        // Hide away the kafkaConsumer, we keep the consumers depending on a name + a topic in a caffeine cache
        ConsumerCacheKey cacheKey = new ConsumerCacheKey(name, topic, keyClass, valueClass);

        // only one poll at a time for the same kafkaConsumer is allowed
        synchronized (cacheKey.getLockObject()) {
            for (int retry = 0; retry < MAX_RETRIES; retry++) {
                try {
                    CachedConsumer cachedConsumer = consumers.get(cacheKey);
                    KafkaConsumer<K, V> consumer = (KafkaConsumer<K, V>) cachedConsumer.kafkaConsumer;

                    // we should only need to seek if the current offset is different
                    if (cachedConsumer.lastPulledOffset < offset - 1) seek(consumer, topic, offset);

                    // and we remember the last offset
                    RecordsWithLastOffset<K, V> recordsAndOffset = mapRecords(consumer.poll(timeOutInMs));
                    consumers.put(cacheKey, cachedConsumer.withOffset(recordsAndOffset.lastOffset));

                    return recordsAndOffset.records;
                } catch (IllegalStateException e) {
                    consumers.invalidate(cacheKey);
                }
            }
        }

        // if everyting fails return and emtpy list  - TODO decide how to handle excelptions
        return new ArrayList<>();
    }

    public void seek(KafkaConsumer<?, ?> consumer, String topic, long offset) {
        consumer.partitionsFor(topic)
                .forEach(partInfo -> consumer.seek(new TopicPartition(topic, partInfo.partition()), offset));
    }

    /**
     * We do not know if this is thread safe. threat it as singleton but synchronize access to this object
     * @return kafka admin client
     */
    public synchronized KafkaAdminClient getAdminClient() {
        if (adminClient == null) adminClient = (KafkaAdminClient) AdminClient.create(kafkaProperties);
        return adminClient;
    }

    public Set<String> listTopics() {
        try {
            return getAdminClient().listTopics()
                                   .names()
                                   .get();
        } catch (InterruptedException | ExecutionException e) {
            // FIXME what should we do? e.printStackTrace();
            return new HashSet<>();
        }
    }

    public void createTopic(int nrOfPartitions, int replicas, String... topics) {
        List<NewTopic> newTopics = Arrays.stream(topics)
                                         .map(topic -> new NewTopic(topic, nrOfPartitions, (short) replicas))
                                         .collect(toList());

        try {
            getAdminClient().createTopics(newTopics)
                            .all()
                            .get();
        } catch (InterruptedException | ExecutionException e) {
            // FIXME what should we do? e.printStackTrace();
        }
    }

    public void deleteTopic(String... topics) {
        try {
            getAdminClient().deleteTopics(Arrays.asList(topics))
                            .all()
                            .get();
        } catch (InterruptedException | ExecutionException e) {
            // FIXME what should we do? e.printStackTrace();
        }
    }

    private <K, V>RecordsWithLastOffset<K, V> mapRecords(ConsumerRecords<K, V> consumerRecords) {
        List<ConsumerRecord<K, V>> records = new ArrayList<>(consumerRecords.count());
        Iterator<ConsumerRecord<K, V>> it = consumerRecords.iterator();
        while (it.hasNext()) records.add(it.next());
        long offset = records.size() > 0 ? records.get(records.size() - 1).offset() : Long.MIN_VALUE;
        return new RecordsWithLastOffset<>(records, offset);
    }

}
