package kic.kafka.simpleclient;

import com.github.benmanes.caffeine.cache.LoadingCache;
import kic.kafka.simpleclient.cache.CacheFactory;
import kic.kafka.simpleclient.cache.ConsumerCacheKey;
import kic.kafka.simpleclient.cache.ProducerCacheKey;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import static java.util.stream.Collectors.toList;

// This class should be a singleton per kafka cluster
public class SimpleKafkaClient {
    private final Properties kafkaProperties;
    private final LoadingCache<ProducerCacheKey, KafkaProducer> producers;
    private final LoadingCache<ConsumerCacheKey, KafkaConsumer> consumers;
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

    public <K, V>ConsumerRecords<K, V> poll(String name, String topic, Class<K> keyClass, Class<V> valueClass, long offset, long timeOutInMs) {
        // Hide away the consumer, we keep the consumers depending on a name + a topic in a caffeine cache
        ConsumerCacheKey cacheKey = new ConsumerCacheKey(name, topic, keyClass, valueClass);
        KafkaConsumer<K, V> consumer = (KafkaConsumer<K, V>) consumers.get(cacheKey);
        seek(consumer, topic, offset);

        // if we have an unresponsive consumer we close it and create a new one
        // do we need to keep an offset somewhere ... ??? we can have different offsets per name
        return consumer.poll(timeOutInMs);
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
}
