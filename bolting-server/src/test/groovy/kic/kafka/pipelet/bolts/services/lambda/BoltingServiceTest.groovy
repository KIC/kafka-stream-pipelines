package kic.kafka.pipelet.bolts.services.lambda

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState
import kic.kafka.pipelet.bolts.persistence.keys.BoltsStateKey
import kic.kafka.pipelet.bolts.services.Daemonify
import org.apache.kafka.clients.consumer.ConsumerRecord
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.Phaser
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

class BoltingServiceTest extends Specification {
    static currentOffset = -1
    static stateId = new BoltsStateKey("pipeline1", "source1", "target1", "service1")
    static sources = [source1:[new ConsumerRecord("source1", 0, 0, "0", "1"),
                               new ConsumerRecord("source1", 0, 1, "1", "2"),
                               new ConsumerRecord("source1", 0, 2, "2", "3"),
                               new ConsumerRecord("source1", 0, 3, "3", "4"),
                               new ConsumerRecord("source1", 0, 4, "4", "5"),
                               new ConsumerRecord("source1", 0, 5, "5", "6"),
                               new ConsumerRecord("source1", 0, 6, "6", "7")]]

    static consumer = { pipelineId, topic ->
        { offset ->
            println("poll for $offset")
            currentOffset = offset
            return offset < sources[topic].size() ? [sources[topic][offset as int]] : [] } as Function }



    def "test bolting service" () {
        // we have the pahses lambda, store, push where we need to deal with exceptions
        def troubles = [false, false, false]
        def result = []
        def targets = []
        def state = new BoltsState(stateId).withNewState("0".bytes, -1)
        def phaser = new Phaser(2)
        def expected = ["1", "3", "6", "10", "15", "21", "28"]

        given:
        def lambda = { s, e ->
            wantTrouble(troubles, 0, "lambda")
            s.withNewState("${s.stateAsString().toInteger() + e.value.toInteger()}".getBytes(), e.offset) } as BiFunction

        def stateUpdater = {s ->
            wantTrouble(troubles, 1, "state update")
            state = s } as Consumer

        def producer = { topic -> {
            newState ->
                wantTrouble(troubles, 2, "push")
                targets << newState.value
                phaser.arriveAndAwaitAdvance() } as Consumer }

        def boltingService = new BoltingService(consumer,
                producer,
                { key -> state },
                stateUpdater,
                Executors.newFixedThreadPool(10),
                new Daemonify())

        boltingService.start()

        when:
        boltingService.enqueue(stateId, lambda)

        // phase 1: no troubles
        arriveAndAwaitPhaserTimeout(phaser, 20000)
        result << (targets == expected[0..0] && state.stateAsString() == expected[0])
        println("round ${phaser.getPhase()} is over.\npassed: ${result.last()}\nstate: $state\n$targets")

        // phase 2: lambda makes trouble
        troubles = [true, false, false]
        arriveAndAwaitPhaserTimeout(phaser, 20000)
        result << (targets == expected[0..1] && state.stateAsString() == expected[1])
        println("round ${phaser.getPhase()} is over.\npassed: ${result.last()}\nstate: $state\n$targets")

        // phase 3: state update makes trouble
        troubles = [false, true, false]
        arriveAndAwaitPhaserTimeout(phaser, 20000)
        result << (targets == expected[0..2] && state.stateAsString() == expected[2])
        println("round ${phaser.getPhase()} is over.\npassed: ${result.last()}\nstate: $state\n$targets")

        // phase 4: producer makes trouble
        troubles = [false, false, true]
        arriveAndAwaitPhaserTimeout(phaser, 20000)
        result << (targets == expected[0..3] && state.stateAsString() == expected[3])
        println("round ${phaser.getPhase()} is over.\npassed: ${result.last()}\nstate: $state\n$targets")

        // rest of the phases make no troubles anymore
        for (int i=phaser.getPhase(); i<sources.source1.size(); i++) {
            arriveAndAwaitPhaserTimeout(phaser, 20000)
            println("round ${phaser.getPhase()} is over.\npassed: ${result.last()}\nstate: $state\n$targets")
        }

        then:
        println(result)
        state.consumerOffset == sources.source1.last().offset()
        state.stateAsString() == expected.last()
        targets == expected
        result == [true,true,true,true]
    }

    def arriveAndAwaitPhaserTimeout(phaser, millies) {
        phaser.arrive()
        phaser.awaitAdvanceInterruptibly(phaser.getPhase(), millies, TimeUnit.MILLISECONDS)
    }

    def wantTrouble(troubles, idx, who) {
        if (troubles[idx]) {
            troubles[idx] = false
            throw new RuntimeException("you are in trouble $who!")
        }
    }
}
