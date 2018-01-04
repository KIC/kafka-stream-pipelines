package kic.kafka.pipelet.bolts.services.lambda

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState
import kic.kafka.pipelet.bolts.persistence.keys.BoltsStateKey
import kic.kafka.pipelet.bolts.services.Daemonify
import org.apache.kafka.clients.consumer.ConsumerRecord
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

class BoltingServiceTest extends Specification {
    static sources = [source1:[new ConsumerRecord("source1", 0, 0, 0, 1), new ConsumerRecord("source1", 0, 0, 1, 2)]]
    static targets = [target1:[]]
    static states = [:]
    static BoltingService thingyService = new BoltingService({ pipelineId, topic -> { offset -> offset < sources[topic].size() ? [sources[topic][offset as int]] : [] } as Function },
                                                             { topic -> { newState -> targets[topic] << newState } as Consumer },
                                                             { key -> states[key] ?: new BoltsState(id: key) },
                                                             { state -> states[state.id] = state },
                                                              Executors.newFixedThreadPool(1),
                                                              new Daemonify())

    def "test add new lambda executing service"() {
        given:
        def a;
        def lambda = { state, event -> state.withNewState(null, event.offset) } as BiFunction
        def stateId = new BoltsStateKey("pipeline1", "source1", "target1", "service1")
        thingyService.add(stateId, lambda)

        when:
        a = 1

        then:
        a == a
        Thread.sleep(2000)
        println(targets)
    }

    def setupSpec() {
        thingyService.start()
    }

    def cleanupSpec() {
        thingyService.shutdown()
    }

}
