package kic.pipeline.sources.spring.services;

import kic.pipeline.sources.spring.entities.JobState;
import kic.pipeline.sources.spring.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobStateService {

    @Autowired
    JobRepository jobRepository;

    public JobState getState(String jobId) {
        JobState job = jobRepository.findOne(jobId);
        return job != null ? job : new JobState(jobId);
        /*Map<String, String> state = new HashMap<>();

        JobState job = jobRepository.findOne(jobId);
        if (job != null) {
            state.put("LAST_KEY", job.getKey());
            state.put("LAST_VALUE", job.getValue());
        }

        return state;*/
    }

    public void setState(JobState state) {
        jobRepository.save(state);
    }
}
