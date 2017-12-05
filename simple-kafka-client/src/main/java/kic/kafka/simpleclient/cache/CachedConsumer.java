package kic.kafka.simpleclient.cache;

import org.apache.kafka.clients.consumer.KafkaConsumer;

public class CachedConsumer {

    public final KafkaConsumer kafkaConsumer;
    public final long lastPulledOffset;

    public CachedConsumer(KafkaConsumer kafkaConsumer) {
        this(kafkaConsumer, 0);
    }

    private CachedConsumer(KafkaConsumer kafkaConsumer, long lastPulledOffset) {
        this.kafkaConsumer = kafkaConsumer;
        this.lastPulledOffset = lastPulledOffset;
    }

    public CachedConsumer withOffset(long offset) {
        return new CachedConsumer(kafkaConsumer, offset);
    }
}
