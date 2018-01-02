package kic.kafka.pipelet.bolts.rest;

import kic.kafka.pipelet.bolts.services.KafkaClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

@RestController
@RequestMapping("/api/v1/topics")
public class Topics {

    @Autowired
    private KafkaClientService kafkaClientService;

    @RequestMapping(path = "", method = RequestMethod.GET)
    private Set<String> boltPipelet() throws MalformedURLException {
        return kafkaClientService.listTopics()
                                 .stream()
                                 .map(this::removeVersionFromTopic)
                                 .collect(toSet());
    }

    private String removeVersionFromTopic(String topic) {
        return topic.split("\\.v")[0];
    }

}
