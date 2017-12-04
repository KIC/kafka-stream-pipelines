package kic.kafka.pipeliet.bolts.rest;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import kic.kafka.pipeliet.bolts.dto.CacheKey;
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/poll")
public class Polling {
    private static final Logger log = LoggerFactory.getLogger(Polling.class);

    @Autowired
    private KafkaServiceFactory kafkaServiceFactory;

    private LoadingCache<CacheKey, KafkaConsumer<Long, String>> consumers = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(59, TimeUnit.SECONDS) // should be less then kafka setting of "heartbeat.interval.ms" so that we can close the kafkaConsumer
            .removalListener(Polling::closeConsumer)
            .build(key -> kafkaServiceFactory.createConsumer(key.pipeline, key.topic, 0)); // FIXME should not be foo!

    @RequestMapping(path = "/{pipelineName}/{topic}", method = GET)
    private PollResult poll(
            @PathVariable String pipelineName,
            @PathVariable String topic,
            @RequestParam(defaultValue = "0") long offset
    ) {
        try {
            KafkaConsumer<Long, String> consumer = consumers.get(new CacheKey(pipelineName, topic));
            consumer.partitionsFor(topic).forEach(pi -> consumer.seek(new TopicPartition(topic, pi.partition()), offset));
            ConsumerRecords<Long, String> poll = consumer.poll(100);

            List<Long> keys = new LinkedList<>();
            List<String> values = new LinkedList<>();
            long lastOffset = offset;

            for (ConsumerRecord<Long, String> record : poll) {
                lastOffset = record.offset();
                keys.add(record.key());
                values.add(record.value());
            }

            return new PollResult(lastOffset, keys, values);
        } catch (Exception e) {
            log.error("Polling exception", e);
            return new PollResult(ExceptionUtils.getStackTrace(e));
        }
    }

    private static void closeConsumer(CacheKey key, KafkaConsumer<Long, String> consumer, RemovalCause cause) {
        try {
            log.info("close kafkaConsumer: {}", key);
            consumer.close();
        } catch (Exception e) {
            log.error("failed to close kafkaConsumer", e);
        }
    }
}
