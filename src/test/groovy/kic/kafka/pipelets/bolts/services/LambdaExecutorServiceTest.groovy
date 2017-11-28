package kic.kafka.pipelets.bolts.services

import com.github.tomakehurst.wiremock.junit.WireMockClassRule
import org.junit.ClassRule
import spock.lang.Specification
import static groovyx.net.http.Method.*

class LambdaExecutorServiceTest extends Specification {
    private static final int WIREMOCK_PORT = 9999;

    @ClassRule
    static final WireMockClassRule WIREMOCK_RULE = new WireMockClassRule(WIREMOCK_PORT);

    def "Execute"() {
        given:
        def lambdaExector = new LambdaExecutorService(new URL(lambda + path))
        def result = null

        when:
        try {
            result = lambdaExector.execute(lambdaMethod)
        } catch (LambdaException le) {
            result = le.httpStatusCode
        }

        then:
        result != null // FIXME

        where:
        lambda                            | path           | lambdaMethod   | query | data   || expected
        "http://not.existing.com"         | "/"            | GET            | [:]   | ""     || null
        "https://google.com"              | "/dasda"       | GET            | [:]   | ""     || null
    }

}

