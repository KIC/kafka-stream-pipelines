package kic.kafka.pipeliet.bolts.demo;

import kic.kafka.pipeliet.bolts.services.KafkaClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication(scanBasePackages={"kic.kafka.pipeliet.bolts"})
public class DemoServer {
    private static final Logger log = LoggerFactory.getLogger(DemoServer.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoServer.class, args);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public CommandLineRunner schedulingRunner(KafkaClientService kafkaClient, TaskExecutor executor) {
        final String demoSourceTopic = "demo-111";

        return (String... args) -> {
            log.info("starting demo pipeline");
            // create a new demo topic
            kafkaClient.createTopic(demoSourceTopic);
            // push some random data to the topic
            executor.execute(new RandomNumberGenerator((key, value) -> kafkaClient.send(demoSourceTopic, key, value)));
            // consume from the topic to prove its working
            executor.execute(new DemoTopicReader(offset -> kafkaClient.poll("demo", demoSourceTopic, Long.class, Double.class, offset, 1000L)));
        };
    }
}
