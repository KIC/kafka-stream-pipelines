package kic.kafka.pipeliet.bolts.rest;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import kic.kafka.pipeliet.bolts.dto.PollResult;
import kic.kafka.pipeliet.bolts.services.KafkaServiceFactory;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/poll")
public class Polling {
    private static final Logger log = LoggerFactory.getLogger(Polling.class);

    @Autowired
    private KafkaServiceFactory kafkaServiceFactory;

    private LoadingCache<String, KafkaConsumer<Long, String>> consumers = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(1, TimeUnit.MINUTES) // should be (a bit) less then kafka setting of "session.timeout.ms" so that we can close the consumer
            .removalListener((k, v, c) -> ((KafkaConsumer) v).close())
            .build(key -> kafkaServiceFactory.createConsumer("foo", key, 0));

    @RequestMapping(path = "/{pipelineName}/{topic}", method = GET)
    private PollResult poll(
            @PathVariable String pipelineName,
            @PathVariable String topic,
            @RequestParam(defaultValue = "0") long offset
    ) {
        try {
            KafkaConsumer<Long, String> consumer = consumers.get(topic);

            // TODO implement seek handling if offset is different from last offset
            consumer.partitionsFor(topic).forEach(pi -> consumer.seek(new TopicPartition(topic, pi.partition()), offset));
            ConsumerRecords<Long, String> poll = consumer.poll(100);
            Map<Long, String> result = new LinkedHashMap<>();
            long lastOffset = offset;

            for (ConsumerRecord<Long, String> record : poll) {
                lastOffset = record.offset();
                result.put(record.key(), record.value());
            }

            return new PollResult(lastOffset, result);
        } catch (Exception e) {
            log.error("Polling exception", e);
            return new PollResult(ExceptionUtils.getStackTrace(e));
        }
    }
}
