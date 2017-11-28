package kic.kafka.pipeliet.bolts.rest;

import kic.kafka.pipeliet.bolts.dto.grafana.AnnotationRequest;
import kic.kafka.pipeliet.bolts.dto.grafana.AnnotationResponse;
import kic.kafka.pipeliet.bolts.dto.grafana.Ping;
import kic.kafka.pipeliet.bolts.dto.grafana.Query;
import kic.kafka.pipeliet.bolts.dto.grafana.Search;
import kic.kafka.pipeliet.bolts.dto.grafana.TimeSeries;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@RestController
@RequestMapping("/")
public class GrafanaJsonSource {
    // simple json datasource for grafana: https://github.com/grafana/simple-json-datasource/tree/master/dist
    private static final Logger log = LoggerFactory.getLogger(GrafanaJsonSource.class);

    @Autowired
    KafkaConsumer<Long, String> kafkaConsumer;

    @Autowired
    KafkaAdminClient adminClient;

    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/", method = GET)
    private Ping ping() {
        return new Ping("OK");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/search", method = POST)
    private List<String> search(@RequestBody Search search) throws InterruptedException, ExecutionException, TimeoutException {
        log.info("search for {}", search); // this only gets populated through the live search text box

        return adminClient.listTopics() // TODO eventually use a kaffein cache to avoid throtteling
                          .listings()
                          .get(1000L, TimeUnit.MILLISECONDS)
                          .stream()
                          .map(TopicListing::name)
                          .filter(name -> search.getTarget().isEmpty() || name.toLowerCase().contains(search.getTarget().toLowerCase()))
                          .collect(toList());
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/query", method = POST)
    private List<TimeSeries> query(@RequestBody Query query) {
        log.debug("query: {}", query);
        // we need to subscribe to the topic
        // we need to poll and filter the result to fit the query range

        Number[] foo1 = new Number[]{1.2f, query.getRange().getFrom().getTime()};
        Number[] foo2 = new Number[]{1.1f, query.getRange().getFrom().getTime() + 120000};
        Number[] foo3 = new Number[]{1.3f, query.getRange().getTo().getTime()};

        return query.getTargets()
                    .stream()
                    .map(target -> new TimeSeries(target.getTarget(), Arrays.asList(foo1, foo2, foo3))) // TODO use a ringbuffer and poll consumer into ring buffer
                    .collect(toList());
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/annotations", method = POST)
    private AnnotationResponse annotations(@RequestBody AnnotationRequest annotationRequest) {
        return new AnnotationResponse(System.currentTimeMillis(), "title", annotationRequest.getAnnotation());
    }

}
