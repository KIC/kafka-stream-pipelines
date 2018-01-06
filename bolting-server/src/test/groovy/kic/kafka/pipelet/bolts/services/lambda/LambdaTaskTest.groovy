package kic.kafka.pipelet.bolts.services.lambda

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState
import kic.kafka.pipelet.bolts.persistence.keys.BoltsStateKey
import kic.kafka.pipelet.bolts.util.Try
import org.apache.kafka.clients.consumer.ConsumerRecord
import spock.lang.Specification

import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

class LambdaTaskTest extends Specification {

    static sources = [source1:[new ConsumerRecord("source1", 0, 0, "0", "1"),
                               new ConsumerRecord("source1", 0, 1, "1", "2"),
                               new ConsumerRecord("source1", 0, 2, "2", "3"),
                               new ConsumerRecord("source1", 0, 3, "3", "4"),
                               new ConsumerRecord("source1", 0, 4, "4", "5"),
                               new ConsumerRecord("source1", 0, 5, "5", "6"),
                               new ConsumerRecord("source1", 0, 6, "6", "7")]]

    def source = {o -> sources.source1} as Function
    def stateId = new BoltsStateKey("pipeline1", "source1", "target1", "service1")
    def state = new BoltsState(stateId)
    def taskId = "testTask"


    def "test call"() {
        given:
        def lambdaFunction = {s, e -> s.withNewState(e.value.getBytes(), e.offset) } as BiFunction
        def lambda = new Lambda({-> state} as Supplier, lambdaFunction)
        def result = []
        def nok = false
        def ok = false

        def target = {e -> result << e.value} as Consumer
        def success = {t -> ok = true } as Consumer
        def failure = {t -> nok = true } as Consumer
        def update = {s -> state = s } as Consumer

        when:
        Try.ignore({ -> new LambdaTask(taskId, lambda, source, update, target, success, failure).call() })

        then:
        println(result)
        ok == true
        nok == false
        state.consumerOffset == sources.source1.last().offset()
        state.stateAsString() == sources.source1.last().value()
        result == sources.source1.collect { cr -> cr.value }
    }

    def "test call with failing lambda"() {
        given:
        def problematicOffset = 3
        def lambdaFunction = {s, e -> if (e.offset == problematicOffset) {
                throw new RuntimeException("haha")
            } else {
                s.withNewState(e.value.getBytes(), e.offset)
            } } as BiFunction
        def lambda = new Lambda({-> state} as Supplier, lambdaFunction)
        def result = []
        def nok = String.class
        def ok = false

        def target = {e -> result << e.value} as Consumer
        def success = {t -> ok = true } as Consumer
        def failure = {t -> nok = t.class} as Consumer
        def update = {s -> state = s } as Consumer

        when:
        Try.ignore({ -> new LambdaTask(taskId, lambda, source, update, target, success, failure).call() })

        then:
        println(result)
        ok == false
        nok == LambdaTask.class
        state.consumerOffset == problematicOffset - 1
        state.stateAsString() == sources.source1[problematicOffset - 1].value()
        result == sources.source1.findAll {cr -> cr.offset() < problematicOffset}
                                 .collect { cr -> cr.value }
    }

    def "test call with failing status"() {
        given:
        def problematicOffset = 3
        def lambdaFunction = {s, e -> s.withNewState(e.value.getBytes(), e.offset) } as BiFunction
        def lambda = new Lambda({-> state} as Supplier, lambdaFunction)
        def result = []
        def nok = String.class
        def ok = false

        def target = {e -> result << e.value} as Consumer
        def success = {t -> ok = true } as Consumer
        def failure = {t -> nok = t.class} as Consumer
        def update = {s -> if (s.consumerOffset == problematicOffset){
                throw new RuntimeException("boom!")
            } else {
                state = s
            } } as Consumer

        when:
        Try.ignore({ -> new LambdaTask(taskId, lambda, source, update, target, success, failure).call() })

        then:
        println(result)
        ok == false
        nok == LambdaTask.class
        state.consumerOffset == problematicOffset - 1
        state.stateAsString() == sources.source1[problematicOffset - 1].value()
        result == sources.source1.findAll {cr -> cr.offset() < problematicOffset}
                                 .collect { cr -> cr.value }
    }

    def "test call with failing push"() {
        given:
        def problematicOffset = 3
        def lambdaFunction = {s, e -> s.withNewState(e.value.getBytes(), e.offset) } as BiFunction
        def lambda = new Lambda({-> state} as Supplier, lambdaFunction)
        def result = []
        def nok = String.class
        def ok = false

        def target = {e ->
            if(e.key.toInteger() == problematicOffset) {
                throw new RuntimeException("bam!")
            } else {
                result << e.value
            }} as Consumer
        def success = {t -> ok = true } as Consumer
        def failure = {t -> nok = t.class} as Consumer
        def update = {s -> state = s } as Consumer

        when:
        Try.ignore({ -> new LambdaTask(taskId, lambda, source, update, target, success, failure).call() })

        then:
        println(result)
        ok == false
        nok == ResendTask.class
        state.consumerOffset == problematicOffset
        state.stateAsString() == sources.source1[problematicOffset].value()
        result == sources.source1.findAll {cr -> cr.offset() < problematicOffset}
                                 .collect { cr -> cr.value }
    }

}
