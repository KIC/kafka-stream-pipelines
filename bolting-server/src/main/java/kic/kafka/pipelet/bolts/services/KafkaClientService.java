package kic.kafka.pipelet.bolts.services;

import kic.kafka.pipelet.bolts.configuration.BoltsConfiguration;
import kic.kafka.simpleclient.SimpleKafkaClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
        return (offset) -> (List) poll(name, topic, Long.class, byte[].class, offset, configuration.getPollTimeout()).entries;
    }

    public Consumer<Map.Entry<Object, byte[]>> newTopicProvider(final String topic) {
        return entry -> send(topic, entry.getKey(), entry.getValue());
    }
}
