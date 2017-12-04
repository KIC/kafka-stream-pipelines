package kic.kafka.simpleclient;

import kic.kafka.embedded.EmbeddedKafaJavaWrapper$;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
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
    public void send() throws Exception {
    }

    @Test
    public void poll() throws Exception {
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

    @AfterClass
    public static void shutdownEmbeddedKafka() throws InterruptedException {
        Thread.sleep(1000);
        EmbeddedKafaJavaWrapper$.MODULE$.stop();
    }
}