package kic.kafka.pipeliet.bolts;

import kic.kafka.embedded.EmbeddedKafaJavaWrapper$;
import kic.kafka.pipeliet.bolts.configuration.BoltsConfiguration;
import kic.kafka.pipeliet.bolts.services.KafkaBoltingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class BoltsServer {
    private static final Logger log = LoggerFactory.getLogger(BoltsServer.class);

    @Autowired
    private BoltsConfiguration configuration;

    public static void main(String[] args) {
        SpringApplication.run(BoltsServer.class, args);
    }

    @Bean
    public CommandLineRunner runKafkaPipeletsBoltsServer(KafkaBoltingService boltingService) {
        return (String... args) -> {
            log.info("using configuration: {}", configuration);

            if (configuration.getEmbeddedKafka().isEnabled()) {
                //Set<String> topics = boltingService.fetchTopics();
                //log.info("available/resumeable topics: {}", topics);
                // start (embedded) kafa server using a conditional https://www.programcreek.com/java-api-examples/index.php?api=org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
                log.info("!! starting embedded kafka !!");
                EmbeddedKafaJavaWrapper$.MODULE$.start(
                        configuration.getEmbeddedKafka().getKafkaPort(),
                        configuration.getEmbeddedKafka().getZookeperPort(),
                        configuration.getKafka()
                );
            }

            boltingService.resumePipeline();
        };
    }

    @PreDestroy
    private void shutdown() {
        if (configuration.getEmbeddedKafka().isEnabled()) {
            log.info("!! stop embedded kafka !!");
            EmbeddedKafaJavaWrapper$.MODULE$.stop();
        }
    }
}
