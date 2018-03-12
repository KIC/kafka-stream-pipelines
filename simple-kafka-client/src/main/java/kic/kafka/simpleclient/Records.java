package kic.kafka.simpleclient;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class Records<K, V> {
    public final List<ConsumerRecord<K, V>> entries;
    public final long firstOffset;
    public final long lastOffset;

    public Records(List<ConsumerRecord<K, V>> records, long firstOffset, long lastOffset) {
        this.entries = records;
        this.firstOffset = firstOffset;
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

    public Optional<ConsumerRecord<K, V>> first() {
        return size() > 0 ? Optional.of(entries.get(0)) : Optional.empty();
    }

    public Optional<ConsumerRecord<K, V>> last() {
        return size() > 0 ? Optional.of(entries.get(size() - 1)) : Optional.empty();
    }

    public long getFirstOffset() {
        return firstOffset;
    }

    public long getLastOffset() {
        return lastOffset;
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

}
