package kic.kafka.simpleclient;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

public class RecordsWithLastOffset<K, V> {
    public final List<ConsumerRecord<K, V>> records;
    public final long lastOffset;

    public RecordsWithLastOffset(List<ConsumerRecord<K, V>> records, long lastOffset) {
        this.records = records;
        this.lastOffset = lastOffset;
    }
}
