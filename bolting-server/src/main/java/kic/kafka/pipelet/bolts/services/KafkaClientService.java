package kic.kafka.pipelet.bolts.services;

import kic.kafka.pipelet.bolts.configuration.BoltsConfiguration;
import kic.kafka.simpleclient.Records;
import kic.kafka.simpleclient.SimpleKafkaClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class KafkaClientService extends SimpleKafkaClient {
    final BoltsConfiguration configuration;

    @Autowired
    public KafkaClientService(BoltsConfiguration configuration) {
        super(configuration.getKafka());
        this.configuration = configuration;
    }

    public Function<Long, List<ConsumerRecord>> newTopicConsumer(final String name, final String topic) {
        return (offset) -> (List) poll(name, topic, String.class, String.class, offset, configuration.getPollTimeout()).entries;
    }

    public Consumer<Map.Entry<String, String>> newTopicProvider(final String topic) {
        return entry -> push(topic, entry.getKey(), entry.getValue());
    }

    public RecordMetadata push(String topic, Object key, Object value) {
        return push(topic, key.toString(), value.toString());
    }

    public RecordMetadata push(String topic, String key, String value) {
        return super.send(topic, key, value);
    }

    public Records<String, String> pull(String name, String topic, long offset, long timeOutInMs) {
        return super.poll(name, topic, String.class, String.class, offset, timeOutInMs);
    }
}
