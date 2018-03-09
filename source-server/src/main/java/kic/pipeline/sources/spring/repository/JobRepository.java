package kic.pipeline.sources.spring.repository;

import kic.pipeline.sources.spring.entities.JobState;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends CrudRepository<JobState, String> {

}