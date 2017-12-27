package kic.kafka.pipelet.bolts.persistence.repositories;

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState;
import kic.kafka.pipelet.bolts.persistence.keys.BoltsStateKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoltsStateRepository extends CrudRepository<BoltsState, BoltsStateKey> {

}
