package kic.pipeline.sources.spring.repository;

import kic.pipeline.sources.spring.entities.JobState;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends CrudRepository<JobState, String> {
    default JobState findOrNew(String jobId) {
        JobState job = findOne(jobId);
        return job != null ? job : new JobState(jobId);
    }
}