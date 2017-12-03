package kic.kafka.pipeliet.bolts.dto;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.Serializable;

public class Event implements Serializable {
    private final Object key;
    private final Object value;
    private final long offset;
    private final int partition;

    public Event(ConsumerRecord<?, ?> record) {
        this.key = record.key();
        this.value = record.value();
        this.offset = record.offset();
        this.partition = record.partition();
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public long getOffset() {
        return offset;
    }

    public int getPartition() {
        return partition;
    }
}
