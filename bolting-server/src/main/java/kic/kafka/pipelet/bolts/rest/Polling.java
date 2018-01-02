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

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/v1/poll")
public class Polling {
    private static final Logger LOG = LoggerFactory.getLogger(Polling.class);

    @Autowired
    private KafkaClientService client;

    @RequestMapping(path = "/offset/{pipelineName}/{topic}/{offset}", method = GET, produces = "application/json")
    private PollResult poll(
            @PathVariable String pipelineName,
            @PathVariable String topic,
            @PathVariable long offset,
            @RequestParam(defaultValue = "1000") long timeout
    ) {
        try {
            Records<String, String> records = client.pull(pipelineName, topic, offset, timeout);
            return new PollResult(records.offsets(), records.keys(), records.values());
        } catch (Exception e) {
            LOG.error("Polling exception", e);
            return new PollResult(ExceptionUtils.getStackTrace(e));
        }
    }

}
