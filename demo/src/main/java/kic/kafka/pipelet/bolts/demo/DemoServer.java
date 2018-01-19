package kic.kafka.pipelet.bolts.demo;

import kic.kafka.embedded.EmbeddedKafaJavaWrapper$;
import kic.kafka.pipelet.bolts.BoltsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages={"kic.kafka.pipelet.bolts"})
public class DemoServer extends BoltsServer {
    private static final Logger log = LoggerFactory.getLogger(DemoServer.class);

    @Autowired
    private DemoServerConfiguration configuration;

    public static void main(String[] args) {
        SpringApplication.run(DemoServer.class, args);
    }

    @Bean
    @Override
    public CommandLineRunner runKafkaPipeletsBoltsServer() {
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

            super.runKafkaPipeletsBoltsServer().run(args);
        };
    }

    @Override
    protected void shutdown() {
        if (configuration.getEmbeddedKafka().isEnabled()) {
            log.info("!! stop embedded kafka !!");
            EmbeddedKafaJavaWrapper$.MODULE$.stop();
        }

        super.shutdown();
    }

}
