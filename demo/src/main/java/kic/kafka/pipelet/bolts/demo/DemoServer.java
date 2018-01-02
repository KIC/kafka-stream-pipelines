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
    @Deprecated
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public CommandLineRunner schedulingRunner(KafkaClientService kafkaClient, TaskExecutor executor, BoltingService boltingService) {
        final RestLambdaWrapper lambdaWrapper = new RestLambdaWrapper(new RestLambda("http://localhost:8080/demo/fold?key=${event.key}&value=${event.value}&state=${state}"));
        final String demoPipeline = "demo-pipeline";

        return (String... args) -> {
            log.info("starting {}", demoPipeline);
        };
    }
}
