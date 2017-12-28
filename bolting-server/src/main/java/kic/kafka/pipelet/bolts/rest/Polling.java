package kic.kafka.pipelet.bolts.rest;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import kic.kafka.pipelet.bolts.dto.PollResult;
import kic.kafka.pipelet.bolts.services.KafkaClientService;
import kic.kafka.simpleclient.Records;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/v1/poll")
public class Polling {
    private static final Logger LOG = LoggerFactory.getLogger(Polling.class);

    @Autowired
    private KafkaClientService client;

    private final LoadingCache<String, PollResult> pollCache = Caffeine.newBuilder()
                                                                       .maximumSize(10_000)
                                                                       .expireAfterAccess(10, TimeUnit.MINUTES)
                                                                       .build(key -> PollResult.emtpy());

    @RequestMapping(path = "/offset/{pipelineName}", method = GET, produces = "application/json")
    private PollResult poll(
            @PathVariable String pipelineName,
            @RequestParam String topic,
            @RequestParam(defaultValue = "1000") long timeout,
            @RequestParam(defaultValue = "0") long offset
    ) {
        try {
            Records<String, String> records = client.pull(pipelineName, topic, offset, timeout);
            return new PollResult(records.offsets(), records.keys(), records.values());
        } catch (Exception e) {
            LOG.error("Polling exception", e);
            return new PollResult(ExceptionUtils.getStackTrace(e));
        }
    }

    @RequestMapping(path = "/cached/{pipelineName}/{cacheId}", method = GET, produces = "application/json")
    private PollResult pollWithCache(
            @PathVariable String pipelineName,
            @PathVariable String cacheId,
            @RequestParam String topic,
            @RequestParam(defaultValue = "1000") long timeout
    ) {
        final String kafkaClientName = pipelineName + "/" + topic + "/" + cacheId;
        PollResult cachedPollResult = pollCache.get(kafkaClientName);

        try {
            PollResult pollResult = cachedPollResult.append(client.pull(kafkaClientName, topic, cachedPollResult.getNextOffset(), timeout));
            pollCache.put(kafkaClientName, pollResult);
            return pollResult;
        } catch (Exception e) {
            LOG.error("Polling exception", e);
            return new PollResult(ExceptionUtils.getStackTrace(e));
        }
    }
}
