package kic.kafka.pipelet.bolts.demo;

import kic.kafka.simpleclient.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class DemoTopicReader implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DemoTopicReader.class);
    private final String prefix;
    private final Function<Long, Records<String, String>> topicPoller;

    public DemoTopicReader(String prefix, Function<Long, Records<String, String>> topicPoller) {
        this.prefix = prefix;
        this.topicPoller = topicPoller;
    }

    @Override
    public void run() {
        long offset = 0;
        while (true) {
            Records<String, String> records = topicPoller.apply(offset);
            if (!records.isEmpty()) {
                records.entries.forEach(cr -> log.info("{} consumed: {}, {} @ {}", prefix, cr.key(), cr.value(), cr.offset()));
                offset = records.lastOffset + 1;
            }
        }
    }
}
