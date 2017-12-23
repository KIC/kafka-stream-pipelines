package kic.kafka.pipeliet.bolts.rest;

import kic.kafka.pipeliet.bolts.services.lambda.RestLambdaWrapper;
import kic.kafka.pipeliet.bolts.services.lambda.Thingy;
import kic.lambda.dispatch.RestLambda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bolt")
public class Bolting {

    // how do we remove pipelets
    // how to we reset pipelets? probabla we need versioned topics? deleting topics is not too easy

    @Autowired
    private Thingy boltingService;

    @RequestMapping(path = "/{pipelineName}", method = RequestMethod.POST)
    private Map boltPipelet(
            @PathVariable String pipelineName,
            @RequestParam(defaultValue = "") String sourceTopic,
            @RequestParam(defaultValue = "") String targetTopic,
            @RequestParam(defaultValue = "GET") String method,
            @RequestBody() String lambdaUrl
    ) throws MalformedURLException {
        // TODO create filter/validator
        if (sourceTopic.isEmpty()) throw new IllegalArgumentException("source topic can not be empty!");
        if (lambdaUrl.isEmpty()) throw new IllegalArgumentException("url can not be empty!");

        final String serviceId = "foo-service.id"; // FIXME serviceID ??
        final RestLambdaWrapper lambdaWrapper = new RestLambdaWrapper(new RestLambda(lambdaUrl));

        boltingService.add(pipelineName,
                           serviceId,
                           sourceTopic,
                           targetTopic,
                           lambdaWrapper);

        // TODO make nice retruns ..
        Map result = new HashMap();
        result.put("status", "OK");
        return result;
    }

}
