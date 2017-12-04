package kic.kafka.simpleclient.cache;

import org.apache.kafka.clients.consumer.KafkaConsumer;

public class CachedConsumer {
    private final KafkaConsumer consumer;
    private final long lastPulledOffset;

    public CachedConsumer(KafkaConsumer consumer) {
        this(consumer, Long.MIN_VALUE);
    }

    private CachedConsumer(KafkaConsumer consumer, long lastPulledOffset) {
        this.consumer = consumer;
        this.lastPulledOffset = lastPulledOffset;
    }

    public CachedConsumer withOffset(long offset) {
        return new CachedConsumer(consumer, offset);
    }
}
