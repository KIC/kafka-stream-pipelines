package kic.kafka.pipeliet.bolts.rest;

import kic.kafka.pipeliet.bolts.services.KafkaBoltingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bolt")
public class Bolting {

    // how do we remove pipelets
    // how to we rest pipelets? probabla we need versioned topics? deleting topics is not too easy

    @Autowired
    private KafkaBoltingService boltingService;

    @RequestMapping(path = "/{pipelineName}", method = RequestMethod.PUT)
    private Map boltPipelet(
            @PathVariable String pipelineName,
            @RequestParam(defaultValue = "") String sourceTopic,
            @RequestParam(defaultValue = "") String targetTopic,
            @RequestParam(defaultValue = "") String lambda
    ) throws MalformedURLException {
        // TODO create filter/validator
        if (sourceTopic.isEmpty()) throw new IllegalArgumentException("source topic can not be empty!");
        if (lambda.isEmpty()) throw new IllegalArgumentException("url can not be empty!");

        boltingService.boltPipelet(pipelineName, sourceTopic, targetTopic, new URL(lambda));

        // TODO make nice retruns ..
        Map result = new HashMap();
        result.put("status", "OK");
        return result;
    }

    @RequestMapping(path = "/foo", method = RequestMethod.GET)
    private Object foo(@RequestParam String url) throws URISyntaxException {
        RestTemplate rt = new RestTemplate();

        return rt.postForObject(new URI(url), "test", String.class);
    }
}
