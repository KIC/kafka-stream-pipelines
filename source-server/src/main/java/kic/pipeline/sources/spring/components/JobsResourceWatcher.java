package kic.pipeline.sources.spring.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import kic.pipeline.sources.dto.Jobs;
import kic.pipeline.sources.spring.entities.JobState;
import kic.pipeline.sources.task.ShellTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class JobsResourceWatcher {
    private static final Logger LOG = LoggerFactory.getLogger(JobsResourceWatcher.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private String lastProcessedJobsResource;

    public void watchJobsResource(Supplier<InputStream> jobsResource, Consumer<Jobs> onJobResourceLoaded) {
        while (true) {
            try {
                Jobs jobSchedules = readJobsResource(jobsResource.get());
                String jobsJson = JSON.writeValueAsString(jobSchedules);

                if (!jobsJson.equals(lastProcessedJobsResource)) {
                    LOG.info("process jobs from resource:\n{}", JSON.writeValueAsString(jobSchedules));

                    onJobResourceLoaded.accept(jobSchedules);

                    // remember resource
                    lastProcessedJobsResource = jobsJson;
                }
            } catch (Exception e) {
                LOG.error("reload of jobsResource failed", e);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Jobs readJobsResource(InputStream jobsResource) throws IOException {
        LOG.trace("read jobresource file {}", jobsResource);
        return JSON.readValue(jobsResource, Jobs.class);
    }

    public void testSingleJob(Jobs jobSchedules, String jobId, String workingDirectory) {
        Jobs.Job job = findJob(jobSchedules, jobId);
        createShelltaskFromJob(job, workingDirectory, (k, v) -> System.out.println(k + "\n" + v)).execute(null);
    }

    private Jobs.Job findJob(Jobs jobs, String id) {
        return jobs.jobs
                   .stream()
                   .filter(job -> job.id.equals(id))
                   .findFirst()
                   .orElseGet(null);
    }

    public ShellTask createShelltaskFromJob(Jobs.Job job, String workingDirectory, BiConsumer<String, String> keyValueConsumer) {
        return createShelltaskFromJob(job, workingDirectory, keyValueConsumer, null, null);
    }

    public ShellTask createShelltaskFromJob(Jobs.Job job,
                                            String workingDirectory,
                                            BiConsumer<String, String> keyValueConsumer,
                                            Function<String, JobState> getJobState,
                                            Consumer<JobState> updateJobState
    ) {
        return new ShellTask(job.id,
                             job.encoding,
                             job.schedule.toString(),
                             new File(workingDirectory).getAbsoluteFile(),
                             job.command,
                             job.keyExtractor,
                             job.valueExtractor,
                             keyValueConsumer,
                             getJobState,
                             updateJobState);
    }


}
