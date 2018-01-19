package kic.kafka.pipelet.bolts.demo;

import kic.kafka.pipelet.bolts.configuration.BoltsConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Primary
@Configuration
@ConfigurationProperties(prefix="bolts")
public class DemoServerConfiguration extends BoltsConfiguration {
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

    public EmbeddedKafka getEmbeddedKafka() {
        return embeddedKafka;
    }

}
