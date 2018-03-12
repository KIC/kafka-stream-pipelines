package kic.pipeline.sources.spring.repository;

import kic.pipeline.sources.spring.entities.KeyValueLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLogRepository extends CrudRepository<KeyValueLog, Integer> {
    
}