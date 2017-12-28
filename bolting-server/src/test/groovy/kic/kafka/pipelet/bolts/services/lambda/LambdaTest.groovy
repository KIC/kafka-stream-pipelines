package kic.kafka.pipelet.bolts.services.lambda


import org.apache.kafka.clients.consumer.ConsumerRecord
import spock.lang.Specification

class LambdaTest extends Specification {

    def "test execute a lambda task"() {
        given:
        def result = null
        def anEvent = new ConsumerRecord("aTopic", 0, 0, 0, 1)
        def aLambdaTask = new Lambda({ -> 1 },
                                         {state, event -> state + event.value},
                                         {newState -> result = newState})

        when:
        aLambdaTask.execute(anEvent)

        then:
        result == 2

    }
}