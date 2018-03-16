package kic.pipeline.sources;

import kic.pipeline.sources.dto.Jobs;
import kic.pipeline.sources.spring.components.JobsResourceWatcher;
import kic.pipeline.sources.spring.components.SchedulerComponent;
import kic.pipeline.sources.spring.entities.JobState;
import kic.pipeline.sources.spring.entities.KeyValueLog;
import kic.pipeline.sources.spring.repository.JobLogRepository;
import kic.pipeline.sources.spring.repository.JobRepository;
import kic.pipeline.sources.task.SimpleProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootApplication
public class CronDaemon implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CronDaemon.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JobRepository jobStateRepository;

    @Autowired
    private JobLogRepository jobLogRepository;

    @Autowired(required = false)
    private JobsResourceWatcher watcher;

    @Autowired(required = false)
    private SchedulerComponent scheduler;

    private Function<Object, String> fetchLastState;
    private Function<Object, String> pushNewestState;

    @Value("${application.jobs.resource}")
    private String jobsResource;

    @Value("${application.jobs.working.dir}")
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
                                                                                                                  decoreateKeyValueConsumerIncludingLog(job.id),
                                                                                                                  decoreateJobStateFetcher(job),
                                                                                                                  decorateExceptionHandler(job.id))));
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

    private Supplier<Map> decoreateJobStateFetcher(Jobs.Job job) {
        if (fetchLastState == null) return HashMap::new;

        // FIXME
        fetchLastState.apply(job);
        String key = null;
        String value = null;

        try {
            jobLogRepository.save(new KeyValueLog(job.id, key, value));
        } catch (DataIntegrityViolationException duplicateKey) {
            LOG.warn("last send message for job {} :: {} :: was not logged", job.id, key, value);
        }

        return null; // FIXME return something
    }

    private BiConsumer<String, String> decoreateKeyValueConsumerIncludingLog(String jobId) {
        HashMap<String, String> lambdaVariabes = new HashMap<>();

        return (key, value) -> {
            LOG.debug("jobId: {} push {} :: {}", jobId, key, value);
            lambdaVariabes.put("KEY", key);
            lambdaVariabes.put("VALUE", value);
            if (pushNewestState != null) pushNewestState.apply(lambdaVariabes);
            jobLogRepository.save(new KeyValueLog(jobId, key, value));
        };
    }

    private Consumer<Exception> decorateExceptionHandler(String jobId) {
        return e -> {
            if (e instanceof SimpleProcess.ProcessException) {
                JobState logState = new JobState(jobId,
                                                 ((SimpleProcess.ProcessException) e).stdOut,
                                                 ((SimpleProcess.ProcessException) e).stdErr,
                                                 ((SimpleProcess.ProcessException) e).msg);

                jobStateRepository.save(logState);
            }
        };
    }
}
