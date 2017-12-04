package kic.kafka.simpleclient;

import kic.kafka.embedded.EmbeddedKafaJavaWrapper$;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class SimpleKafkaClientTest {
    static SimpleKafkaClient client;

    @BeforeClass
    public static void startEmbeddeKafka() throws IOException {
        Properties properties = new Properties();
        properties.load(SimpleKafkaClientTest.class.getResourceAsStream("kafka-test.properties"));
        System.out.println(properties);
        EmbeddedKafaJavaWrapper$.MODULE$.start(10010, 10020, properties);
        client = new SimpleKafkaClient(properties);
    }

    @Test
    public void getAdminClient() throws Exception {
        assertNotNull(client.getAdminClient().describeCluster());
    }

    @Test
    public void createTopic() throws Exception {
        final String topic = "create-test-topic";
        client.createTopic(1,1, topic);
        assertTrue(client.listTopics().contains(topic));
    }

    @Test
    public void deleteTopic() throws Exception {
        final String topic = "create-temptest-topic";
        client.createTopic(1,1,topic);
        assertTrue(client.listTopics().contains(topic));
        client.deleteTopic(topic);
        assertFalse(client.listTopics().contains(topic));
    }


    @Test
    public void sendAndPoll() throws Exception {
        final String topic = "create-test-push-topic";
        client.createTopic(1,1, topic);
        client.send(topic, 1L, "test message");
        List<ConsumerRecord<Long, String>> records = client.poll("test-client", topic, Long.class, String.class,0, 1000);
        assertTrue(records.size() > 0);
    }


    // we need to test some corner cases

    @Test // first listen on a topic and then create it and send something afterwards
    public void testListenOnNotPreExisitngTopic() {
        final String topic = "create-test-poll-before-send-topic";
        List<ConsumerRecord<Long, String>> records = client.poll("test-client", topic, Long.class, String.class,0, 1000);
        assertTrue(records.isEmpty());

        client.createTopic(1,1, topic);
        client.send(topic, 1L, "test message");

        records = client.poll("test-client", topic, Long.class, String.class,0, 1000);
        assertTrue(records.size() > 0);
    }

    @Test // create send and poll for a message using an object which has no defined serializer
    public void restDefaultObjectSerialization() {
        final String topic = "create-objects-topic";
        client.createTopic(1,1, topic);
        client.send(topic, 1L, new ArrayList<>());
        List<ConsumerRecord<Long, ArrayList>> records = client.poll("test-client", topic, Long.class, ArrayList.class, 0, 1000);
        assertTrue(records.size() > 0);
        assertEquals(0, records.iterator().next().value().size());
    }

    // if a kafkaConsumer misses to send a heartbeat and gets stalled we want to detect, close and recreate the kafkaConsumer
    public void testUnresponsiveClient() {
        // TODO how can we test that
    }

    @AfterClass
    public static void shutdownEmbeddedKafka() throws InterruptedException {
        Thread.sleep(1000);
        EmbeddedKafaJavaWrapper$.MODULE$.stop();
    }
}