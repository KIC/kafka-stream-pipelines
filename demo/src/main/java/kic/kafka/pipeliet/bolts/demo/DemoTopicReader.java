package kic.kafka.pipeliet.bolts.demo;

import kic.kafka.simpleclient.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class DemoTopicReader implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DemoTopicReader.class);
    private final Function<Long, Records<Long, Double>> topicPoller;

    public DemoTopicReader(Function<Long, Records<Long, Double>> topicPoller) {
        this.topicPoller = topicPoller;
    }

    @Override
    public void run() {
        long offset = 0;
        while (true) {
            Records<Long, Double> records = topicPoller.apply(offset);
            records.entries.forEach(cr -> log.info("polled: {}, {} @ {}", cr.key(), cr.value(), cr.offset()));
            offset = records.lastOffset + 1;
        }
    }
}
