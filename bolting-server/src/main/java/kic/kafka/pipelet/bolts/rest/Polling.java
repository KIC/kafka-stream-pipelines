package kic.kafka.pipeliet.bolts.rest;

import kic.kafka.pipeliet.bolts.dto.PollResult;
import kic.kafka.pipeliet.bolts.services.KafkaClientService;
import kic.kafka.simpleclient.Records;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/v1/poll")
public class Polling {
    private static final Logger log = LoggerFactory.getLogger(Polling.class);

    @Autowired
    private KafkaClientService client;

    @RequestMapping(path = "/{pipelineName}/{topic}", method = GET)
    private PollResult poll(
            @PathVariable String pipelineName,
            @PathVariable String topic,
            @RequestParam(defaultValue = "500") long timeout,
            @RequestParam(defaultValue = "0") long offset
    ) {
        try {
            Records<Long, String> records = client.poll(pipelineName, topic, Long.class, String.class, offset, timeout);
            return new PollResult(records.lastOffset, records.keys(), records.values());
        } catch (Exception e) {
            log.error("Polling exception", e);
            return new PollResult(ExceptionUtils.getStackTrace(e));
        }
    }

}
