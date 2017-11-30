package kic.lambda.dispatch

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

class LambdaExecutorService implements LambdaExecutor {
    final HTTPBuilder lambda

    LambdaExecutorService(URL lambda) {
        this.lambda = new HTTPBuilder(lambda)
    }

    @Override
    def execute(Method method, Object payload, long key, String value) {
        return execute(method, payload, [(key): value])
    }

    def execute(Method method, Object data = null, Map queryParameter = [:]) {
        def lambdaResponseStatus = -1
        def lambdaResponse = ""

        try {
            lambda.request(method) {
                uri.query = queryParameter
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers.Accept = 'application/json'
                send ContentType.JSON, data

                response.success = { resp, reader ->
                    lambdaResponseStatus = resp.statusLine.statusCode
                    lambdaResponse = reader.text
                }
            }
        } catch (Exception e) {
            throw new LambdaException(lambdaResponseStatus, lambdaResponse, e)
        }

        return lambdaResponse
    }

}


class LambdaException extends Exception {
    public final int httpStatusCode

    LambdaException(int httpStatusCode, String var1, Throwable exception) {
        super("HTTP: $httpStatusCode: $var1", exception)
        this.httpStatusCode = httpStatusCode
    }

}


interface LambdaExecutor {
    def execute(Method method, Object payload, long key, String value)
}