package kic.kafka.pipelet.bolts.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer {
    private final String MESSAGE_BROKER_PREFIX = "/topic";
    private final String WEBSOCKET_PREFIX = "/sockjs-node";
    private final String REQUEST_PREFIX = "/";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WEBSOCKET_PREFIX)
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(MESSAGE_BROKER_PREFIX);
        config.setApplicationDestinationPrefixes(REQUEST_PREFIX);
    }

}
