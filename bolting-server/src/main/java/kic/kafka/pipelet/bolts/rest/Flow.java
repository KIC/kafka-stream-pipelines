package kic.kafka.pipelet.bolts.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flow")
public class Flow {

    /* Usage:
     *  curl -H "Content-Type: text/plain" \
     *       -X POST 'http://localhost:8080/api/v1/bolt/$pieline/$service/$srctopic/$tgttopic/POST' \
     *       -d 'http://localhost:8080/api/v1/flow/${event.key}' -d '${event.value}'
     */
    @RequestMapping(path = "/{key}", method = RequestMethod.POST, consumes = "text/plain", produces = "text/plain")
    private String postFlow(
            @PathVariable String key,
            @RequestBody() String value
    ) {
        return value;
    }

}
