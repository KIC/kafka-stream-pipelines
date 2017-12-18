package kic.kafka.pipeliet.bolts.persistence.keys;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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

    public BoltsStateKey(String inboundTopic, String outboundTopic, String service) {
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
    public String toString() {
        return "BoltsStateKey{" +
                "inboundTopic='" + inboundTopic + '\'' +
                ", outboundTopic='" + outboundTopic + '\'' +
                ", service=" + service +
                '}';
    }
}
