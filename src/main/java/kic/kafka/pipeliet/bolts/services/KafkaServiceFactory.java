package kic.kafka.pipeliet.bolts.services;

import kic.kafka.pipeliet.bolts.configuration.BoltsConfiguration;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Properties;

@Service
public class KafkaServiceFactory {
    private KafkaAdminClient adminClient = null;

    @Autowired
    BoltsConfiguration configuration;

    /**
     * The kafkaConsumer is NOT thread-safe. See Multi-threaded Processing for more details.
     * @return kafka kafkaConsumer
     */
    public KafkaConsumer<Long, String> createConsumer(String pipeline) {
        Properties props = (Properties) configuration.getKafka().clone();
        props.setProperty("group.id", pipeline);
        return new KafkaConsumer<>(props);
    }

    /**
     * The kafkaConsumer is NOT thread-safe. See Multi-threaded Processing for more details.
     * @param topic topic to subscribe to
     * @param offset offet to start from
     * @return kafka kafkaConsumer
     */
    public KafkaConsumer<Long, String> createConsumer(String pipeline, String topic, long offset) {
        KafkaConsumer<Long, String> consumer = createConsumer(pipeline);
        consumer.subscribe(Arrays.asList(topic));
        // consumers are lazily initialized so we need to poll before we can seek
        consumer.poll(0);
        consumer.partitionsFor(topic).forEach(pi -> consumer.seek(new TopicPartition(topic, pi.partition()), offset));
        return consumer;
    }


    /**
     * The producer is thread safe and sharing a single producer instance across threads will generally be faster
     * @return kafka priducer
     */
    @Bean
    public KafkaProducer<Long, String> creareProducer() {
        return new KafkaProducer<>(configuration.getKafka());
    }

    /**
     * We do not know if this is thread safe. threat it as singleton but synchronize access to this object
     * @return kafka admin client
     */
    @Bean
    public KafkaAdminClient getAdminClient() {
        if (adminClient == null) adminClient = (KafkaAdminClient) AdminClient.create(configuration.getKafka());
        return adminClient;
    }

    /**
     * Create a new topic
     *
     * @param topic
     * @return New Topic
     */
    public void createTopic(String topic, int nrOfPartitions, int replicas) {
        getAdminClient().createTopics(Arrays.asList(new NewTopic(topic, nrOfPartitions, (short) replicas)));
    }

    public void deleteTopic(String topic) {
        getAdminClient().deleteTopics(Arrays.asList(topic));
    }

    public void recreateTopic(String topic, int nrOfPartitions, int replicas) {
        deleteTopic(topic);
        createTopic(topic, nrOfPartitions, replicas);
    }
}
