package kic.kafka.pipelet.bolts.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix="bolts")
public class BoltsConfiguration {

    private Properties kafka;
    private long pollTimeout = 333L;

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
                ", kafka=" + kafka +
                ", pollTimeout=" + pollTimeout +
                '}';
    }
}
