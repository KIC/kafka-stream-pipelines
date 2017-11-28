package kic.kafka.pipeliet.bolts.services.bolting;

public interface Producer<K, V> {
    void send(String topic, K key, V value);
}
