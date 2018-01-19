package kic.kafka.simpleclient;

import kic.kafka.embedded.EmbeddedKafaJavaWrapper$;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        client.createTopic(topic);
        assertTrue(client.listTopics().contains(topic));
    }

    @Test
    public void deleteTopic() throws Exception {
        final String topic = "create-temptest-topic";
        client.createTopic(topic);
        assertTrue(client.listTopics().contains(topic));
        client.deleteTopic(topic);
        assertFalse(client.listTopics().contains(topic));
    }


    @Test
    public void sendAndPoll() throws Exception {
        final String topic = "create-test-push-topic";
        client.createTopic(topic);
        client.send(topic, 1L, "test message");
        Records<Long, String> records = client.poll("test-client", topic, Long.class, String.class,0, 1000);
        assertTrue(records.size() > 0);
    }


    // we need to test some corner cases

    @Test // first listen on a topic and then create it and send something afterwards
    public void testListenOnNotPreExisitngTopic() {
        final String topic = "create-test-poll-before-send-topic";
        Records<Long, String> records = client.poll("test-client", topic, Long.class, String.class,0, 1000);
        assertTrue(records.isEmpty());

        client.createTopic(topic);
        client.send(topic, 1L, "test message");

        records = client.poll("test-client", topic, Long.class, String.class,0, 1000);
        assertTrue(records.size() > 0);
    }

    @Test // create send and poll for a message using an object which has no defined serializer
    public void testDefaultObjectSerialization() {
        final String topic = "create-objects-topic";
        client.createTopic(topic);
        client.send(topic, 1L, new ArrayList<>());
        Records<Long, ArrayList> records = client.poll("test-client", topic, Long.class, ArrayList.class, 0, 1000);
        assertTrue(records.size() > 0);
        assertEquals(0, records.iterator().next().value().size());
    }

    @Test
    public void testStream() throws InterruptedException {
        final String topic = "test-stream-topic";
        final String message = "test message";
        final long key = 1L;
        final CountDownLatch latch = new CountDownLatch(1);
        final Map result = new HashMap();
        final KafkaStreams kafkaStreams = client.streaming("test-stream")
                                                .start(b -> b.stream(topic).foreach((k, v) -> {
                                                    result.put(k, v);
                                                    latch.countDown();
                                                }));

        client.send(topic, key, message);
        latch.await();

        System.out.println(result);
        assertEquals(1, result.size());
        assertEquals(message, result.get(key));
        kafkaStreams.close(1, TimeUnit.SECONDS);
        kafkaStreams.cleanUp();
    }

    @Test
    public void testDeleteAll() {
        final String topic = "some-bugous-topic";
        client.createTopic(topic);
        assertTrue(client.listTopics().size() > 0);
        client.deleteAllTopics();
        assertTrue(client.listTopics().size() <= 0);
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