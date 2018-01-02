package kic.kafka.pipelet.bolts.persistence.keys;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

@Embeddable
public class BoltsStateKey implements Serializable {
    @Column(nullable = false)
    String inboundTopic;
    @Column(nullable = false)
    String outboundTopic;
    @Column(nullable = false)
    String service;

    protected BoltsStateKey() {
    }

    public BoltsStateKey(@NotNull String inboundTopic, @NotNull String outboundTopic, @NotNull String service) {
        this.inboundTopic = inboundTopic;
        this.outboundTopic = outboundTopic;
        this.service = service;
    }

    public String getInboundTopic() {
        return inboundTopic;
    }

    public void setInboundTopic(String inboundTopic) {
        this.inboundTopic = inboundTopic;
    }

    public String getOutboundTopic() {
        return outboundTopic;
    }

    public void setOutboundTopic(String outboundTopic) {
        this.outboundTopic = outboundTopic;
    }

    public URL getService() throws MalformedURLException {
        return new URL(service);
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoltsStateKey that = (BoltsStateKey) o;

        if (!inboundTopic.equals(that.inboundTopic)) return false;
        if (!outboundTopic.equals(that.outboundTopic)) return false;
        return service.equals(that.service);
    }

    @Override
    public int hashCode() {
        int result = inboundTopic.hashCode();
        result = 31 * result + outboundTopic.hashCode();
        result = 31 * result + service.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BoltsStateKey{" +
                "inboundTopic='" + inboundTopic + '\'' +
                ", outboundTopic='" + outboundTopic + '\'' +
                ", service=" + service +
                '}';
    }
}
