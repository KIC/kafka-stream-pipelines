package kic.kafka.pipelet.bolts.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix="bolts")
public class BoltsConfiguration {
    public static class EmbeddedKafka {
        private boolean enabled = false;
        private int zookeperPort = 2181;
        private int kafkaPort = 9092;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getZookeperPort() {
            return zookeperPort;
        }

        public void setZookeperPort(int zookeperPort) {
            this.zookeperPort = zookeperPort;
        }

        public int getKafkaPort() {
            return kafkaPort;
        }

        public void setKafkaPort(int kafkaPort) {
            this.kafkaPort = kafkaPort;
        }

        @Override
        public String toString() {
            return "EmbeddedKafka{" +
                    "enabled=" + enabled +
                    ", zookeperPort=" + zookeperPort +
                    ", kafkaPort=" + kafkaPort +
                    '}';
        }
    }
    private final EmbeddedKafka embeddedKafka = new EmbeddedKafka();
    private Properties kafka;
    private long pollTimeout = 333L;

    public EmbeddedKafka getEmbeddedKafka() {
        return embeddedKafka;
    }

    public Properties getKafka() {
        return kafka;
    }

    public void setKafka(Properties kafka) {
        this.kafka = kafka;
    }

    public long getPollTimeout() {
        return pollTimeout;
    }

    public void setPollTimeout(long pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    @Override
    public String toString() {
        return "BoltsConfiguration{" +
                "embeddedKafka=" + embeddedKafka +
                ", kafka=" + kafka +
                ", pollTimeout=" + pollTimeout +
                '}';
    }
}
