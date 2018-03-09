package kic.pipeline.sources;

import kic.pipeline.sources.spring.components.JobsResourceWatcher;
import kic.pipeline.sources.spring.components.SchedulerComponent;
import kic.pipeline.sources.spring.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

@SpringBootApplication
public class CronDaemon implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CronDaemon.class);
    //private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JobRepository jobStateService;

    @Autowired(required = false)
    private JobsResourceWatcher watcher;

    @Autowired(required = false)
    private SchedulerComponent scheduler;

    @Autowired(required = false)
    @Qualifier("KeyValueConsumer")
    private BiConsumer<String, String> keyValueConsumer;

    @Value("${jobs.resource}")
    private String jobsResource;

    @Value("${application.working.dir}")
    private String workingDirectory;

    public static void main(String[] args) {
        SpringApplication.run(CronDaemon.class, args);
    }

    @Override
    public void run(String... args) {
        if (watcher != null && scheduler != null) {
            if (args.length > 1 && args[0].contains("test")) {
                LOG.info("test job {}", args[1]);
                watcher.watchJobsResource(this::getJobResourceStream, jobs -> watcher.testSingleJob(jobs, args[1], workingDirectory));
            } else if (scheduler != null) {
                LOG.info("start job scheduler");
                scheduler.start();
                watcher.watchJobsResource(this::getJobResourceStream,
                                          jobs -> scheduler.updateSchedules(jobs,
                                                                            job -> watcher.createShelltaskFromJob(job,
                                                                                                                  workingDirectory,
                                                                                                                  keyValueConsumer,
                                                                                                                  jobStateService::findOrNew,
                                                                                                                  jobStateService::save)));
            }
        }
    }

    private InputStream getJobResourceStream() {
        try {
            return context.getResource(jobsResource).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void overrideKeyValueConsumer() {
        keyValueConsumer = (k, v) -> System.out.println("\n---\nkey: " + k + "\nvalue: " + v + "\n---\n");
    }


}
