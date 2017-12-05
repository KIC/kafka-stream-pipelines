package kic.kafka.pipeliet.bolts.services;

import kic.kafka.pipeliet.bolts.configuration.BoltsConfiguration;
import kic.kafka.simpleclient.SimpleKafkaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KafkaClientService extends SimpleKafkaClient {

    @Autowired
    public KafkaClientService(BoltsConfiguration configuration) {
        super(configuration.getKafka());
    }

}
