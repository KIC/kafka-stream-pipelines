package kic.lambda.dispatch

import groovy.text.GStringTemplateEngine
import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException
import groovyx.net.http.Method
import org.apache.http.util.EntityUtils

import java.util.function.Function

import static groovyx.net.http.ContentType.BINARY
import static groovyx.net.http.ContentType.JSON

@Slf4j
class RestExecutor {
    final GStringTemplateEngine templateEngine = new GStringTemplateEngine();
    final Map bindings
    final boolean urlEncode;

    RestExecutor(Map<String, String> bindings = [:], boolean urlEncode = true) {
        this.bindings = bindings
        this.urlEncode = urlEncode
    }

    def <T>T executeTemplate(Method method, String url, String payload, ContentType contentType, Function<String, T> responseProcessor) {
        return responseProcessor.apply(executeTemplate(method, url, payload, contentType))
    }

    RestResponse executeTemplate(Method method, String url, String payload = "", ContentType contentType = JSON) {
        if (log.isDebugEnabled()) log.debug("{} url {} : {}", method, url, bindings)
        def lambdaUrl = templateEngine.createTemplate(url)
                                      .make(bindings)
                                      .toString()
                                      .toURL()

        def lambdaBody = templateEngine.createTemplate(payload)
                                       .make(bindings)
                                       .toString()

        return execute(method, lambdaUrl, lambdaBody.isEmpty() ? null : lambdaBody, contentType)
    }

    static RestResponse execute(Method method, URL lambdaUrl, Object payload = null, ContentType contentType = JSON, Map queryParameter = [:], Map extraHeaders = [:]) {
        // lambdaUrl.query = queryParameter
        log.info("exec: $method:$lambdaUrl\n$payload")
        def lambda = new HTTPBuilder(lambdaUrl)
        def lambdaResponseStatus = -1
        def lambdaResponse = null

        try {
            lambda.request(method, BINARY) {
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers << extraHeaders
                send contentType, payload

                response.success = { resp, reader ->
                    lambdaResponseStatus = resp.statusLine.statusCode
                    lambdaResponse = new RestResponse(lambdaResponseStatus,
                                                      EntityUtils.getContentCharSet(resp.entity) ?: "UTF-8",
                                                      reader.bytes)
                }
            }
        } catch (HttpResponseException hre) {
            throw new RestException(hre.statusCode, lambdaResponse?.toString(), hre)
        } catch (Exception e) {
            throw new RestException(lambdaResponseStatus, lambdaResponse?.toString(), e)
        }

        if (log.isDebugEnabled()) log.debug("respose($lambdaResponseStatus):\n$lambdaResponse")
        return lambdaResponse
    }

}


class RestException extends RuntimeException {
    public final int httpStatusCode

    RestException(int httpStatusCode, String var1, Throwable exception) {
        super("HTTP: $httpStatusCode: $var1", exception)
        this.httpStatusCode = httpStatusCode
    }

}

class RestResponse implements Serializable {
    public final int httpStatus
    public final String encoding
    public final byte[] bytes

    RestResponse(int httpStatus, String encoding, byte[] bytes) {
        this.httpStatus = httpStatus
        this.encoding = encoding
        this.bytes = bytes
    }

    @Override
    String toString() {
        return new String(bytes, encoding)
    }
}
