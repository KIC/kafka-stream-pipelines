package kic.kafka.simpleclient;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Records<K, V> {
    public final List<ConsumerRecord<K, V>> entries;
    public final long lastOffset;

    public Records(List<ConsumerRecord<K, V>> records, long lastOffset) {
        this.entries = records;
        this.lastOffset = lastOffset;
    }

    public Iterator<ConsumerRecord<K, V>> iterator() {
        return entries.iterator();
    }

    public List<Long> offsets() {
        return entries.stream().map(cr -> cr.offset()).collect(toList());
    }

    public List<K> keys() {
        return entries.stream().map(cr -> cr.key()).collect(toList());
    }

    public List<V> values() {
        return entries.stream().map(cr -> cr.value()).collect(toList());
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
