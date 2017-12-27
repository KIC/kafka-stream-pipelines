package kic.kafka.pipelet.bolts.rest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/proxy")
public class Proxy {
    private static final String AUTH = "bbc8c017-064f-495f-906d-78720b50d7e6";

    @RequestMapping(path = "/", method = RequestMethod.GET)
    private String proxy(@RequestParam String url, @RequestParam String auth, @RequestBody String body) throws URISyntaxException {
        if (AUTH.equals(auth)) {
            RestTemplate rt = new RestTemplate();
            return rt.postForObject(new URI(url), body, String.class);
        } else {
            return null;
        }
    }
}
