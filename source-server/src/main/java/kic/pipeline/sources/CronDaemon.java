package kic.pipeline.sources;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.TaskExecutor;
import kic.pipeline.sources.dto.Jobs;
import kic.pipeline.sources.task.ShellTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SpringBootApplication
public class CronDaemon implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CronDaemon.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Scheduler scheduler = new Scheduler();
    private final Map<String, ShellTask> tasks = new HashMap<>();
    private String lastProcessedJobsResource;

    @Autowired
    private ApplicationContext context;

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
        if (args.length > 1 && args[0].contains("test")) {
            LOG.info("test job {}", args[1]);
            watchJobsResource(jobs -> testSingleJob(jobs, args[1]));
        } else {
            LOG.info("start job scheduler");
            startScheduler();
            watchJobsResource(this::updateSchedules);
        }
    }

    private void startScheduler() {
        scheduler.setDaemon(true);
        scheduler.addSchedulerListener(new SchedulerListener() {
            @Override
            public void taskLaunching(TaskExecutor executor) {
                LOG.info("start {}", executor.getTask());
            }

            @Override
            public void taskSucceeded(TaskExecutor executor) {
                LOG.info("done {}", executor.getTask());
            }

            @Override
            public void taskFailed(TaskExecutor executor, Throwable exception) {
                LOG.error("failed", exception);
            }
        });
        scheduler.start();
    }

    private void watchJobsResource(Consumer<Jobs> onJobResourceLoaded) {
        while (true) {
            try {
                Jobs jobSchedules = readJobsResource();
                String jobsJson = JSON.writeValueAsString(jobSchedules);

                if (!jobsJson.equals(lastProcessedJobsResource)) {
                    LOG.info("process jobs from resource:\n{}", JSON.writeValueAsString(jobSchedules));

                    onJobResourceLoaded.accept(jobSchedules);

                    // remember resource
                    lastProcessedJobsResource = jobsJson;
                }
            } catch (Exception e) {
                LOG.error("reload of " + jobsResource + " failed", e);
                tasks.values().forEach(job -> LOG.info("scheduled job: {}", job));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void testSingleJob(Jobs jobSchedules, String jobId) {
        overrideKeyValueConsumer();
        Jobs.Job job = findJob(jobSchedules, jobId);
        createShelltaskFromJob(job).execute(null);
    }

    private void overrideKeyValueConsumer() {
        keyValueConsumer = (k, v) -> System.out.println("\n---\nkey: " + k + "\nvalue: " + v + "\n---\n");
    }

    private void updateSchedules(Jobs jobSchedules) {
        HashSet<String> unschedule = new HashSet<>(tasks.keySet());
        for (Jobs.Job job : jobSchedules.jobs) {
            ShellTask task = tasks.computeIfAbsent(job.id, id -> createShelltaskFromJob(job));
            unschedule.remove(job.id);

            task.execute(null); // FIXME just now make this a test
            if (task.getScheduleId() == null) {
                // schedule new jobs
                LOG.info("schedule new job {}", job.id);
                task.setScheduleId(scheduler.schedule(job.schedule.toString(), task));
            } else if (!task.getSchedule().equals(job.schedule)) {
                // update existing jobs
                LOG.info("update existing job {}", job.id);
                scheduler.reschedule(task.getScheduleId(), job.schedule.toString());
            }
        }

        // unschedule obsolete jobs
        unschedule.forEach(id -> {
            LOG.info("unschedule job {}", id);
            scheduler.deschedule(tasks.remove(id).getScheduleId());
        });
    }

    private Jobs readJobsResource() throws IOException {
        LOG.trace("read jobresource file {}", jobsResource);
        return JSON.readValue(context.getResource(jobsResource).getInputStream(), Jobs.class);
    }

    private ShellTask createShelltaskFromJob(Jobs.Job job) {
        return new ShellTask(job.id,
                             job.encoding,
                             job.schedule.toString(),
                             new File(workingDirectory).getAbsoluteFile(),
                             job.command,
                             job.keyExtractor,
                             job.valueExtractor,
                             keyValueConsumer);
    }

    private Jobs.Job findJob(Jobs jobs, String id) {
        return jobs.jobs
                   .stream()
                   .filter(job -> job.id.equals(id))
                   .findFirst()
                   .orElseGet(null);
    }
}
