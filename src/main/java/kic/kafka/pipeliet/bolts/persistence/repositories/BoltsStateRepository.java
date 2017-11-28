package kic.kafka.pipeliet.bolts.persistence.repositories;

import kic.kafka.pipeliet.bolts.persistence.entities.BoltsState;
import kic.kafka.pipeliet.bolts.persistence.keys.BoltsStateKey;
import org.springframework.data.repository.CrudRepository;

public interface BoltsStateRepository extends CrudRepository<BoltsState, BoltsStateKey> {

}
