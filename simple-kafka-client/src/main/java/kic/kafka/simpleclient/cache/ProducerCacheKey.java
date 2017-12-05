package kic.kafka.simpleclient.cache;

public class ProducerCacheKey {
    public final String keyClass;
    public final String valueClass;

    public ProducerCacheKey(Class<?> keyClass, Class<?> valueClass) {
        this.keyClass = keyClass.getName();
        this.valueClass = valueClass.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProducerCacheKey that = (ProducerCacheKey) o;

        if (!keyClass.equals(that.keyClass)) return false;
        return valueClass.equals(that.valueClass);
    }

    @Override
    public int hashCode() {
        int result = keyClass.hashCode();
        result = 31 * result + valueClass.hashCode();
        return result;
    }
}
