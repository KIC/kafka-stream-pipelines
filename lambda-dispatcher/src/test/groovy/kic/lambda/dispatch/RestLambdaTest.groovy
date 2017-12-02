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
        http.get("/health", { request, response -> "healthy" })
    })

    @Test
    void testApply() {
        def restLambda = new RestLambda("http://localhost:4567/ping")
        // TODO we should have an add one lambda where we can test newState = currentState + event
        println(restLambda.apply("", null))
    }


    @AfterClass
    static void shutDown() {

    }
}
