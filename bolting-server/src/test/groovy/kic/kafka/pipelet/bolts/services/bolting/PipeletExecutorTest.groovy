package kic.kafka.pipelet.bolts.services.bolting

import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import spock.lang.Specification

class PipeletExecutorTest extends Specification {
    def consumer = new MockConsumer(OffsetResetStrategy.EARLIEST)

    def "test call"() {
        given:
        // TODO if we got the wiremock server running
        def a = ""

        when:
        // TODO implement stimulus
        a.toString()

        then:
        // TODO implement assertions
        1 == 1

    }
}
