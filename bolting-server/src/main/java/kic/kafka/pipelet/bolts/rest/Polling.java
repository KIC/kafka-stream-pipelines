package kic.kafka.pipelet.bolts.rest;

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

import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/v1/poll")
public class Polling {
    private static final Logger LOG = LoggerFactory.getLogger(Polling.class);
    private static final String SALT = UUID.randomUUID().toString();

    @Autowired
    private KafkaClientService client;

    @RequestMapping(path = "/{pipelineName}/{topic}", method = GET, produces = "application/json")
    private PollResult poll(
            @PathVariable String pipelineName,
            @PathVariable String topic,
            @RequestParam(defaultValue = "1000") long timeout,
            @RequestParam(defaultValue = "0") long offset
    ) {
        try {
            Records<String, String> records = client.pull(SALT + pipelineName, topic, offset, timeout);
            return new PollResult(records.offsets(), records.keys(), records.values());
        } catch (Exception e) {
            LOG.error("Polling exception", e);
            return new PollResult(ExceptionUtils.getStackTrace(e));
        }
    }

}
