package kic.kafka.pipelet.bolts.services.lambda

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState
import org.apache.kafka.clients.consumer.ConsumerRecord
import spock.lang.Specification

class LambdaExecutorTest extends Specification {

    def "test call"() {
        given:
        def stateMaker = {value -> new BoltsState(state: "$value".bytes)}
        def stateInitializer = { -> stateMaker(0)}
        def stateUpdater = {state, event -> stateMaker(state.stateAsString().toInteger() + event.value.toInteger())}
        def stateExtractor = {event, state -> }
        def aLambdaTask = new Lambda(stateInitializer, stateUpdater, stateExtractor)

        def result = []
        def eventMaker = {offset, key, value -> new ConsumerRecord("aTopic", 0, offset, "$key", "$value")}
        def eventSource = {offset -> [1:0, 2:1, 3:2, 4:3].collect {k, v -> eventMaker(v, k, v)}}
        def eventTarget = {newState -> result << new String(newState.value)}
        def executor = new LambdaTask("test", aLambdaTask, eventSource, eventTarget, {Task t -> }, {Task t -> })

        when:
        executor.call()

        then:
        println(result)
        result == ["0", "1", "3", "6"]
    }

}
