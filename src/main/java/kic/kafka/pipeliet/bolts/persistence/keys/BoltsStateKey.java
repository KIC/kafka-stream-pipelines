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
    String restEndpoint;

    protected BoltsStateKey() {
    }

    public BoltsStateKey(String inboundTopic, String outboundTopic, String restEndpoint) {
        this.inboundTopic = inboundTopic;
        this.outboundTopic = outboundTopic;
        this.restEndpoint = restEndpoint;
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

    public URL getRestEndpoint() throws MalformedURLException {
        return new URL(restEndpoint);
    }

    public void setRestEndpoint(String restEndpoint) {
        this.restEndpoint = restEndpoint;
    }

    @Override
    public String toString() {
        return "BoltsStateKey{" +
                "inboundTopic='" + inboundTopic + '\'' +
                ", outboundTopic='" + outboundTopic + '\'' +
                ", restEndpoint=" + restEndpoint +
                '}';
    }
}
