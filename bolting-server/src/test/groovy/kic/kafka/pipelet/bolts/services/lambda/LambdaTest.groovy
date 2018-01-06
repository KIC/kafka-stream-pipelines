package kic.kafka.pipelet.bolts.services.lambda


import org.apache.kafka.clients.consumer.ConsumerRecord
import spock.lang.Specification

import java.util.function.BiFunction
import java.util.function.Supplier

class LambdaTest extends Specification {

    def "test execute a lambda task"() {
        given:
        def anEvent = new ConsumerRecord("aTopic", 0, 0, 0, 1)
        def aLambdaTask = new Lambda({ -> 1 } as Supplier, { state, event -> state + event.value} as BiFunction)

        when:
        def result = aLambdaTask.execute(anEvent)

        then:
        result == 2

    }
}
