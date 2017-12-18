package kic.lambda.dispatch

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.util.function.BiFunction
import groovyx.net.http.Method
import groovyx.net.http.ContentType
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class RestLambda implements BiFunction<String, Object, String> {
    final Method httpMethod
    final String urlTemplate
    final String payloadTemplate
    final ContentType payloadContentType

    RestLambda(String urlTemplate, Method httpMethod = GET, String payloadTemplate = "", ContentType payloadContentType = JSON) {
        this.httpMethod = httpMethod
        this.urlTemplate = urlTemplate
        this.payloadTemplate = payloadTemplate ?: ""
        this.payloadContentType = payloadContentType
    }

    @Override
    String apply(String currentState, Object event) {
        RestExecutor executor = new RestExecutor([event: event, state: currentState])
        return executor.executeTemplate(httpMethod, urlTemplate, payloadTemplate, payloadContentType).toString()
    }

    static String toJson(object) {
        return JsonOutput.toJson(object);
    }

    static Map jsonToMap(String json) {
        return new JsonSlurper().parseText(json)
    }
}
