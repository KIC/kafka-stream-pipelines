package kic.kafka.pipelets.bolts.services

import com.github.tomakehurst.wiremock.junit.WireMockClassRule
import kic.lambda.dispatch.RestException
import kic.lambda.dispatch.RestExecutor
import org.junit.ClassRule
import spock.lang.Specification
import static groovyx.net.http.Method.*

class LambdaExecutorServiceTest extends Specification {
    private static final int WIREMOCK_PORT = 9999;

    @ClassRule
    static final WireMockClassRule WIREMOCK_RULE = new WireMockClassRule(WIREMOCK_PORT);

    def "Execute"() {
        given:
        def lambdaExector = new RestExecutor()
        def result = null

        when:
        try {
            result = lambdaExector.execute(lambdaMethod, new URL(lambda + path))
        } catch (RestException le) {
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

