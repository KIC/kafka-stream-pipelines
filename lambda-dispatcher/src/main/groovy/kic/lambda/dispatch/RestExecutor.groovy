package kic.lambda.dispatch

import groovy.text.GStringTemplateEngine
import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException
import groovyx.net.http.Method
import static groovyx.net.http.ContentType.*
import static java.net.URLEncoder.*

@Slf4j
class RestExecutor {
    final GStringTemplateEngine templateEngine = new GStringTemplateEngine();
    final Map bindings
    final boolean urlEncode;

    RestExecutor(Map<String, String> bindings = [:], boolean urlEncode = true) {
        this.bindings = bindings
        this.urlEncode = urlEncode
    }

    def executeTemplate(Method method, String url, String payload = "", ContentType contentType = JSON) {
        def lambdaUrl = templateEngine.createTemplate(url)
                                      .make(bindings.collectEntries { entry -> [(entry.key) : (urlEncode ? encode(entry.value) : entry.value)] })
                                      .toString()
                                      .toURL()

        def lambdaBody = templateEngine.createTemplate(payload)
                                       .make(bindings)
                                       .toString()

        return execute(method, lambdaUrl, lambdaBody, contentType)
    }

    def static execute(Method method, URL lambdaUrl, Object payload = null, ContentType contentType = JSON, Map queryParameter = [:], Map extraHeaders = [:]) {
        // lambdaUrl.query = queryParameter
        log.info("exec: $method:$lambdaUrl\n$payload")
        def lambda = new HTTPBuilder(lambdaUrl)
        def lambdaResponseStatus = -1
        def lambdaResponse = ""

        try {
            lambda.request(method) {
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers << extraHeaders
                send contentType, payload

                response.success = { resp, reader ->
                    lambdaResponseStatus = resp.statusLine.statusCode
                    lambdaResponse = reader.text
                }
            }
        } catch (HttpResponseException hre) {
            throw new RestException(hre.statusCode, lambdaResponse, hre)
        } catch (Exception e) {
            throw new RestException(lambdaResponseStatus, lambdaResponse, e)
        }

        return lambdaResponse
    }

}


class RestException extends Exception {
    public final int httpStatusCode

    RestException(int httpStatusCode, String var1, Throwable exception) {
        super("HTTP: $httpStatusCode: $var1", exception)
        this.httpStatusCode = httpStatusCode
    }

}

/*
i want something like
execute("GET:http://lala/some/endpoint?key=$KEY&value=$VALUE&last_result=$LAST_RESULT)
execute("POST|application/json|$PAYLOAD:http://lala/some/endpoint?key=$KEY&value=$VALUE)

or we just define a data structure how we want our
 */
