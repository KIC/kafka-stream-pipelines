package kic.kafka.simpleclient.stream;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.ForeachAction;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KfakaSimpleStream implements Runnable {
    private final StreamsBuilder builder = new StreamsBuilder();
    private final Properties properties;
    private KafkaStreams streams;

    public KfakaSimpleStream(Properties properties) {
        this.properties = properties;
    }

    public StreamsBuilder builder() {
        return builder;
    }

    @Override
    public void run() {
        try {
            this.streams = new KafkaStreams(builder.build(), properties);
            streams.start();
        } catch (Exception ie) {
            streams.close();
        }
    }

    public void stop(final long timeout, final TimeUnit timeUnit) {
        if (streams != null) {
            streams.close(timeout, timeUnit);
        }
    }

    public Runnable runnableForEach(String topic, final ForeachAction action) {
        builder.stream(topic).foreach(action);
        return this;
    }

}
