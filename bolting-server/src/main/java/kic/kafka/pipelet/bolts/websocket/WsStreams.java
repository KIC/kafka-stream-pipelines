package kic.kafka.pipelet.bolts.websocket;

import kic.kafka.pipelet.bolts.dto.TaskDescription;
import kic.kafka.pipelet.bolts.services.KafkaClientService;
import kic.kafka.pipelet.bolts.services.lambda.BoltingService;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@Controller
@EnableScheduling
public class WsStreams {
    private static final Logger LOG = LoggerFactory.getLogger(WsStreams.class);

    private final Map<String, Map<String, KafkaStreams>> websocketStreams = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private BoltingService boltingService;

    @Autowired
    private KafkaClientService client;


    @SubscribeMapping("/topic/{pipelineId}/{topic}/{sessionId}")
    private String subscribe(
            SimpMessageHeaderAccessor headerAccessor,
            @DestinationVariable String pipelineId,
            @DestinationVariable String topic,
            @DestinationVariable String sessionId
    ) {
        LOG.debug("subscribe: {}/{}/{}", pipelineId, topic, sessionId);
        if (!headerAccessor.getSessionId().equals(sessionId)) throw new IllegalArgumentException("invalid session id");

        // make sure that the topic exists, this is needed for kafka streams!
        client.createTopic(topic);

        // start stream
        websocketStreams.computeIfAbsent(sessionId, sid -> new ConcurrentHashMap<>())
                        .put(topic, streamTopic(pipelineId, topic, sessionId));

        return "{}";
    }

    @EventListener
    public void onApplicationEvent(SessionConnectedEvent event) {
        StompHeaderAccessor stompHeaders = StompHeaderAccessor.wrap(event.getMessage());
        LOG.debug("connect: {}", stompHeaders);
    }

    @EventListener
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor stompHeaders = StompHeaderAccessor.wrap(event.getMessage());
        LOG.debug("disconnect: {}", stompHeaders);
        closeStream(event.getSessionId());
    }

    @Scheduled(fixedRate = 10000)
    public void broadcastTasks() {
        template.convertAndSend("/topic/tasks", boltingService.getTasks()
                                                                        .stream()
                                                                        .map(TaskDescription::new)
                                                                        .collect(toList()));
    }


    public KafkaStreams streamTopic(String pipelineId, String sourceTopic, String sessionId) {
        final String clientId = sessionId + "!" +  pipelineId + "!" + sourceTopic;
        final String destination = "/topic/" + pipelineId + "/" + sourceTopic + "/" + sessionId;
        return client.streaming(clientId)
                     .start(builder -> builder.stream(sourceTopic).foreach((k, v) -> sendMessage(destination, k, v)));

    }

    private void closeStream(String key) {
        LOG.info("evicting: {}", key);
        websocketStreams.computeIfAbsent(key, k -> new HashMap<>())
                        .values()
                        .forEach(s -> { s.close(); s.cleanUp();});
    }

    private void sendMessage(String topic, Object key, Object value) {
        LOG.info("send to {}, {}: {}", topic, key, value);
        // TODO we need a special way of generating the json since we expect key and value to already be a primitive or a json
        template.convertAndSend(topic, "{\"k\": " + key + ", \"v\": " + value +"}");
    }

    @PreDestroy
    private void cleanUp() {
        LOG.info("sutting down all streams!");
        new ArrayList<>(websocketStreams.keySet()).forEach(this::closeStream);
    }
}
