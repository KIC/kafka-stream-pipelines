package kic.kafka.pipelet.bolts.rest;

import kic.kafka.pipelet.bolts.persistence.keys.BoltsStateKey;
import kic.kafka.pipelet.bolts.services.lambda.BoltingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bolt")
public class Bolting {

    // how do we remove pipelets
    // how to we reset pipelets? probabla we need versioned topics? deleting topics is not too easy

    @Autowired
    private BoltingService boltingService;

    @RequestMapping(path = "/{pipelineName}/{serviceId}/{sourceTopic}/{targetTopic}/{method}", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    private Map boltPipelet(
            @PathVariable String pipelineName,
            @PathVariable String serviceId,
            @PathVariable String sourceTopic,
            @PathVariable String targetTopic,
            @PathVariable String method,
            @RequestBody() String lambdaCurlTemplate
    ) throws MalformedURLException {
        // TODO create filter/validator
        if (sourceTopic.isEmpty()) throw new IllegalArgumentException("source topic can not be empty!");
        if (lambdaCurlTemplate.isEmpty()) throw new IllegalArgumentException("url can not be empty!");

        // this is a very simple workaround to sperate url and payload.
        // later we want to implement a cURL like syntax and parse the line as if it was a curl command on the shell
        String[] urlAndPayloadTemplate = lambdaCurlTemplate.split("\\s+\\-d\\s+");
        String urlTemplate = urlAndPayloadTemplate[0];
        String payloadTemplate = urlAndPayloadTemplate.length > 1 ? urlAndPayloadTemplate[1] : "";
        BoltsStateKey id = new BoltsStateKey(pipelineName, sourceTopic, targetTopic, serviceId);

        // TODO move this into the bolting service
        final String contentType = "text/plain";
        boltingService.add(id, urlTemplate, method, payloadTemplate, contentType);

        // TODO make nice retruns ..
        Map result = new HashMap();
        result.put("status", "OK");
        return result;
    }

}
