package kic.kafka.simpleclient.cache;

import java.util.UUID;

public class ConsumerCacheKey {
    private static final String NAME_SPANCE = "%" + UUID.randomUUID().toString();
    private final String lock;
    public final String thread;
    public final String name;
    public final String topic;
    public final String keyClass;
    public final String valueClass;

    public <K, V> ConsumerCacheKey(String name, String topic, Class<K> keyClass, Class<V> valueClass) {
        this.lock = (NAME_SPANCE + "|" + name + "|" + topic).intern();
        this.thread = Thread.currentThread().getName(); // Kafka Consumers are NOT threadsafe!
        this.name = name;
        this.topic = topic;
        this.keyClass = keyClass.getName();
        this.valueClass = valueClass.getName();
    }

    public String getLockObject() {
        return lock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsumerCacheKey that = (ConsumerCacheKey) o;

        if (!name.equals(that.name)) return false;
        if (!topic.equals(that.topic)) return false;
        if (!thread.equals(that.thread)) return false;
        if (!keyClass.equals(that.keyClass)) return false;
        return valueClass.equals(that.valueClass);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + topic.hashCode();
        result = 31 * result + thread.hashCode();
        result = 31 * result + keyClass.hashCode();
        result = 31 * result + valueClass.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ConsumerCacheKey{" +
                "lock='" + lock + '\'' +
                ", name='" + name + '\'' +
                ", topic='" + topic + '\'' +
                ", keyClass='" + keyClass + '\'' +
                ", valueClass='" + valueClass + '\'' +
                ", thread='" + thread + '\'' +
                '}';
    }
}
