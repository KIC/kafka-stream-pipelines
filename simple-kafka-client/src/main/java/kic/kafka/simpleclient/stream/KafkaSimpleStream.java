package kic.kafka.simpleclient.stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.function.Consumer;

public class KafkaSimpleStream {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaSimpleStream.class);
    private final Properties properties;

    public KafkaSimpleStream(Properties properties) {
        this.properties = properties;
    }

    public KafkaStreams start(Consumer<StreamsBuilder> builder) {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        builder.accept(streamsBuilder);
        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), properties);

        try {
            LOG.info("start stream: {}", properties.getProperty(StreamsConfig.APPLICATION_ID_CONFIG));
            streams.start();
            return streams;
        } catch (Exception e) {
            LOG.error("error in stream: {}\n{}", properties.getProperty(StreamsConfig.APPLICATION_ID_CONFIG),
                                                 ExceptionUtils.getMessage(e));
            try {
                // not sure if this is needed
                streams.close();
                streams.cleanUp();
            } catch (Exception ignore) {}

            throw e;
        }
    }

}
