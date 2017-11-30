package kic.kafka.pipeliet.bolts.dto;

import javax.validation.constraints.NotNull;

public class CacheKey {
    public final String pipeline;
    public final String topic;

    public CacheKey(@NotNull String pipeline, @NotNull String topic) {
        this.pipeline = pipeline;
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheKey cacheKey = (CacheKey) o;

        if (!pipeline.equals(cacheKey.pipeline)) return false;
        return topic.equals(cacheKey.topic);
    }

    @Override
    public int hashCode() {
        int result = pipeline.hashCode();
        result = 31 * result + topic.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CacheKey{" +
                "pipeline='" + pipeline + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }
}
