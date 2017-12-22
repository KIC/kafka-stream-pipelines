package kic.kafka.pipeliet.bolts.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class Demo { // FIXME move this into a demo module
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    @RequestMapping(path = "/fold", method = RequestMethod.GET)
    private String foldLeft(@RequestParam(defaultValue = "") String key, @RequestParam(defaultValue = "") String value) {
        log.info("demo key = {}, value = {}", key, value);
        return "";
    }

}
