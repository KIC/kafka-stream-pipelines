package kic.kafka.pipelet.bolts.services.lambda

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState
import org.apache.commons.lang3.SerializationUtils
import org.apache.kafka.clients.consumer.ConsumerRecord
import spock.lang.Specification

class LambdaTaskExecutorTest extends Specification {

    def "test call"() {
        given:
        def stateMaker = {value -> new BoltsState(state: SerializationUtils.serialize(value))}
        def stateInitializer = { -> stateMaker(0)}
        def stateUpdater = {state, event -> stateMaker(SerializationUtils.deserialize(state.state) + event.value)}
        def stateExtractor = {state -> }
        def aLambdaTask = new LambdaTask(stateInitializer, stateUpdater, stateExtractor)

        def result = []
        def eventMaker = {offset, key, value -> new ConsumerRecord("aTopic", 0, offset, key, value)}
        def eventSource = {offset -> [1:0, 2:1, 3:2, 4:3].collect {k, v -> eventMaker(v, k, v)}}
        def eventTarget = {newState -> result << SerializationUtils.deserialize(newState.value)}
        def executor = new LambdaTaskExecutor(aLambdaTask, eventSource, eventTarget)

        when:
        executor.call()

        then:
        println(result)
        result == [0, 1, 3, 6]
    }

}
