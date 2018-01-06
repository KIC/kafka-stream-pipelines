package kic.kafka.pipelet.bolts.rest;

import kic.kafka.pipelet.bolts.services.KafkaClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/v1/push")
public class Push {
    private static final Logger LOG = LoggerFactory.getLogger(Polling.class);

    @Autowired
    private KafkaClientService client;

    @RequestMapping(path = "/{topic}/{key}", method = POST, consumes = "text/plain", produces = "application/json")
    private Long poll(
            @PathVariable String topic,
            @PathVariable String key,
            @RequestBody() String value
    ) {
        if (topic.isEmpty()) throw new IllegalArgumentException("source topic can not be empty!");
        if (key.isEmpty()) throw new IllegalArgumentException("key can not be empty!");
        if (value == null) throw new IllegalArgumentException("value can not be null!");

        return client.push(topic, key, value).offset();
    }

}

