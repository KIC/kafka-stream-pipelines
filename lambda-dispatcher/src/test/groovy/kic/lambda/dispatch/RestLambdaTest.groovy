package kic.lambda.dispatch

import junit.testing.rule.SparkServerRule
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import static groovy.json.JsonOutput.*
import static groovyx.net.http.ContentType.*
import static com.github.tomakehurst.wiremock.client.WireMock.*
import static groovyx.net.http.Method.*


class RestLambdaTest {

    @ClassRule
    public static final SparkServerRule SPARK_SERVER = new SparkServerRule({http ->
        http.get("/ping", { request, response  -> "pong" })
        http.get("/accumulate", { request, response ->
            request.queryParams("state").toDouble() + request.queryParams("value").toDouble()
        })
        http.post("/accumulate", { request, response ->
            request.body().toDouble() + request.queryParams("value").toDouble()
        })
    })

    @Test
    void testSimpleApply() {
        def restLambda = new RestLambda("http://localhost:4567/ping")
        assert restLambda.apply(null, null) == "pong"
    }

    @Test
    void testAccumulatingApplyViaGet() {
        def accumulateEventsLambda = new RestLambda('http://localhost:4567/accumulate?state=$state&key=&value=$event')
        def state = "1"

        for(int i=0; i<10; i++) {
            state = accumulateEventsLambda.apply(state, 1d)
            assert (i + 2d) == state.toDouble()
        }
    }

    @Test
    void testAccumulatingApplyViaPost() {
        def accumulateEventsLambda = new RestLambda('http://localhost:4567/accumulate?key=&value=$event', POST, '$state')
        def state = "1"

        for(int i=0; i<10; i++) {
            state = accumulateEventsLambda.apply(state, 1d)
            assert (i + 2d) == state.toDouble()
        }
    }

}
