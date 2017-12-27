package kic.kafka.pipelet.bolts.services.lambda

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState
import org.apache.kafka.clients.consumer.ConsumerRecord
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
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
                                              Executors.newFixedThreadPool(1))

    def "test add new lambda executing service service"() {
        given:
        def a;
        def lambda = { state, event -> state.withNewState(null, event.offset) } as BiFunction
        thingyService.add("pipeline1", "service1", "source1", "target1", lambda)

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

    // dummy executor service for testing
    private static ExecutorService currentThreadExecutorService() {
        ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy();
        return new ThreadPoolExecutor(0, 1, 0L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), callerRunsPolicy) {
            @Override void execute(Runnable command) {
                callerRunsPolicy.rejectedExecution(command, this);
            }
        };
    }
}
