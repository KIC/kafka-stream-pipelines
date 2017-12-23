package kic.kafka.pipeliet.bolts.demo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoRest {
    private static final Logger log = LoggerFactory.getLogger(DemoRest.class);

    // curl -X POST "http://localhost:8080/bolt/demoPipeline?sourceTopic=test-111&targetTopic=test-222" -d "http://localhost:8080/demo/fold?key=${event.key}&value=${event.value}"

    @RequestMapping(path = "/fold", method = RequestMethod.GET)
    private String foldLeft(@RequestParam(defaultValue = "") String key,
                            @RequestParam(defaultValue = "") String value,
                            @RequestParam(defaultValue = "1") String state
    ) {
        log.info("demo key = {}, value = {}", key, value);
        return "" + (Double.parseDouble(state) * (1d + Double.parseDouble(value)));
    }

}
