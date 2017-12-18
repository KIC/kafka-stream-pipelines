package kic.kafka.pipeliet.bolts.services.bolting;

import java.net.URISyntaxException;
import java.net.URL;

// we should add the offset as well and then this would be basically an entity ...
@Deprecated
public class Pipelet {
    private final String sourceTopic;
    private final String targetTopic;
    private final URL lambda;
    private Object state;

    public Pipelet(String sourceTopic, String targetTopic, URL url, Object state) throws URISyntaxException {
        this.sourceTopic = sourceTopic;
        this.targetTopic = targetTopic;
        this.lambda = url;
        this.state = state;
    }

    public void updateState(Object state) {
        this.state = state;
    }

    public String getSourceTopic() {
        return sourceTopic;
    }

    public String getTargetTopic() {
        return targetTopic;
    }

    public URL getLambda() {
        return lambda;
    }

    public Object getState() {
        return state;
    }

}
