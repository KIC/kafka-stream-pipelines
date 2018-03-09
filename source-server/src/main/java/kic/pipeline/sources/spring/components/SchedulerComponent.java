package kic.pipeline.sources.spring.components;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.TaskExecutor;
import kic.pipeline.sources.dto.Jobs;
import kic.pipeline.sources.task.ShellTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

@Component
@Profile("!unittest")
public class SchedulerComponent {
    private static final Logger LOG = LoggerFactory.getLogger(SchedulerComponent.class);
    private final Scheduler scheduler = new Scheduler();
    private final Map<String, ShellTask> tasks = new HashMap<>();

    public void start() {
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

    public void updateSchedules(Jobs jobSchedules, Function<Jobs.Job, ShellTask> createShellTask) {
        HashSet<String> obsoleteJobs = new HashSet<>(tasks.keySet());
        for (Jobs.Job job : jobSchedules.jobs) {
            ShellTask task = tasks.computeIfAbsent(job.id, id -> createShellTask.apply(job));
            obsoleteJobs.remove(job.id);

            if (task.getScheduleId() == null) {
                // schedule new jobs
                schedule(job.schedule.toString(), task);
            } else if (!task.getSchedule().equals(job.schedule)) {
                // update existing jobs
                reschedule(task, job.schedule.toString());
            }
        }

        // unschedule obsolete jobs
        obsoleteJobs.forEach(id -> {
            deschedule(tasks.remove(id));
        });
    }


    private void deschedule(ShellTask task) {
        LOG.info("unschedule job {}", task.getJobId());
        scheduler.deschedule(task.getScheduleId());
    }

    private void reschedule(ShellTask task, String schedule) {
        LOG.info("update existing job {}", task.getJobId());
        scheduler.reschedule(task.getScheduleId(), schedule);
    }

    private void schedule(String schedule, ShellTask task) {
        LOG.info("schedule new job {}", task.getJobId());
        task.setScheduleId(scheduler.schedule(schedule, task));
    }


}
