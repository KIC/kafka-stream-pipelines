package kic.kafka.simpleclient;

import java.util.Properties;

public class PropertiesExtender {
    private final Properties properties;

    public PropertiesExtender(Properties properties) {
        this.properties = (Properties) properties.clone();
    }

    public PropertiesExtender with(String key, String value) {
        properties.setProperty(key, value);
        return this;
    }

    public Properties extend() {
        return properties;
    }
}
