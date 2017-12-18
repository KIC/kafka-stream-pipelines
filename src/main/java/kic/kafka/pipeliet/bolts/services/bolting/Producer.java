package kic.kafka.pipeliet.bolts.services.bolting;

@Deprecated
public interface Producer<K, V> {
    void send(String topic, K key, V value);
}
