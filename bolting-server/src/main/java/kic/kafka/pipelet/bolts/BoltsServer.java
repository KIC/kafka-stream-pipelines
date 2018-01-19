package kic.kafka.pipelet.bolts;

import kic.kafka.pipelet.bolts.configuration.BoltsConfiguration;
import kic.kafka.pipelet.bolts.services.KafkaClientService;
import kic.kafka.pipelet.bolts.services.lambda.BoltingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import java.util.Arrays;

@SpringBootApplication
public class BoltsServer {
    private static final Logger LOG = LoggerFactory.getLogger(BoltsServer.class);

    @Autowired
    private Environment environment;

    @Autowired
    private BoltsConfiguration configuration;

    @Autowired KafkaClientService clientService;

    @Autowired
    protected BoltingService boltingService;

    public static void main(String[] args) {
        SpringApplication.run(BoltsServer.class, args);
    }

    @Bean
    public CommandLineRunner runKafkaPipeletsBoltsServer() {
        return (String... args) -> {
            LOG.info("using configuration: {}", configuration);
            this.boltingService.start();
        };
    }


    @PreDestroy
    @Profile("develop") // sadly does not work: https://jira.spring.io/browse/SPR-12433
    private void cleanUpKafka() {
        // dirty workaround for https://jira.spring.io/browse/SPR-12433
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> p.equalsIgnoreCase("develop"))) {
            clientService.deleteAllTopics();
        }
    }

    @PreDestroy
    protected void shutdown() {
        LOG.info("shutting down bolt service");
        boltingService.shutdown();
    }

}
