package kic.kafka.pipelet.bolts.demo;

import kic.kafka.pipelet.bolts.services.lambda.RestLambdaWrapper;
import kic.kafka.pipelet.bolts.services.KafkaClientService;
import kic.kafka.pipelet.bolts.services.lambda.BoltingService;
import kic.lambda.dispatch.RestLambda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication(scanBasePackages={"kic.kafka.pipelet.bolts"})
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
    public CommandLineRunner schedulingRunner(KafkaClientService kafkaClient, TaskExecutor executor, BoltingService boltingService) {
        final RestLambdaWrapper lambdaWrapper = new RestLambdaWrapper(new RestLambda("http://localhost:8080/demo/fold?key=${event.key}&value=${event.value}&state=${state}"));
        final String demoPipeline = "demo-pipeline";
        final String demoService = "demo-fold-service";
        final String demoSourceTopic = "demo-111";
        final String demoFoldTopic = "demo-fold-111";

        return (String... args) -> {
            log.info("starting {}", demoPipeline);

            // create a new demo topic
            kafkaClient.createTopic(demoSourceTopic);

            // push some random data to the topic
            executor.execute(new RandomNumberGenerator((key, value) -> kafkaClient.push(demoSourceTopic, key, value)));

            // consume from the topic to prove its working
            executor.execute(new DemoTopicReader("source", offset -> kafkaClient.pull(demoPipeline, demoSourceTopic, offset, 1000L)));

            // bolt a demo service to the source
            boltingService.add(demoPipeline, demoService, demoSourceTopic, demoFoldTopic, lambdaWrapper);

            // consume from the new topic to prove its working
            executor.execute(new DemoTopicReader(demoFoldTopic, offset -> kafkaClient.pull(demoPipeline, demoFoldTopic, offset, 1000L)));
        };
    }
}
