package kic.kafka.simpleclient.cache;

public class ConsumerCacheKey {
    public final String name;
    public final String topic;
    public final String keyClass;
    public final String valueClass;

    public <K, V> ConsumerCacheKey(String name, String topic, Class<K> keyClass, Class<V> valueClass) {
        this.name = name;
        this.topic = topic;
        this.keyClass = keyClass.getName();
        this.valueClass = valueClass.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsumerCacheKey that = (ConsumerCacheKey) o;

        if (!name.equals(that.name)) return false;
        if (!topic.equals(that.topic)) return false;
        if (!keyClass.equals(that.keyClass)) return false;
        return valueClass.equals(that.valueClass);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + topic.hashCode();
        result = 31 * result + keyClass.hashCode();
        result = 31 * result + valueClass.hashCode();
        return result;
    }
}
