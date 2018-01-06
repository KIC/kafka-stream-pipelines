package kic.kafka.pipelet.bolts.services.lambda

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState
import org.apache.kafka.clients.consumer.ConsumerRecord
import spock.lang.Specification

import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Supplier

class LambdaExecutorTest extends Specification {

    def "test call"() {
        given:
        def state = new BoltsState(state: "0".bytes)
        def stateInitializer = { -> state} as Supplier
        def lambda = {s, e -> state.withNewState("${s.stateAsString().toInteger() + e.value.toInteger()}".bytes, e.offset)} as BiFunction
        def aLambdaTask = new Lambda(stateInitializer, lambda)

        def result = []
        def eventMaker = {offset, key, value -> new ConsumerRecord("aTopic", 0, offset, "$key", "$value")}
        def eventSource = {offset -> [1:0, 2:1, 3:2, 4:3].collect {k, v -> eventMaker(v, k, v)}}
        def eventTarget = {newState -> result << new String(newState.value)}
        def stateUpdater = {s -> state = s} as Consumer
        def executor = new LambdaTask("test", aLambdaTask, eventSource, stateUpdater, eventTarget, {Task t -> }, {Task t -> })

        when:
        executor.call()

        then:
        println(result)
        result == ["0", "1", "3", "6"]
    }

}
