package kic.lambda.dispatch

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import static groovy.json.JsonOutput.*
import static groovyx.net.http.ContentType.*


import static com.github.tomakehurst.wiremock.client.WireMock.*
import static groovyx.net.http.Method.*

class RestExecutorTest {
    static final int PORT = 18000
    static WireMockServer wireMockServer
    static bindings = [aa: "A", bb: "&B"]

    @BeforeClass
    static void setUpOnce() {
        wireMockServer = new WireMockServer(PORT)
        wireMockServer.start()

        wireMockServer.stubFor(get(urlEqualTo("/successful/get"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody("success!")
                )
        )

        wireMockServer.stubFor(post("/successful/post?a=A&b=%26B")
                .withQueryParam("a", new EqualToPattern("A"))
                .withQueryParam("b", new EqualToPattern("&B"))
                .withRequestBody(new ContainsPattern("&B"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody("success!")
                )
        )
    }

    @Test
    void testExecuteSuccess() {
        assert RestExecutor.execute(GET, "http://localhost:$PORT/successful/get".toURL()) == "success!"
    }

    @Test
    void testExecuteFail() {
        try {
            RestExecutor.execute(GET, "http://localhost:$PORT/unsuccessful/get".toURL())
        } catch (RestException re) {
            assert re.httpStatusCode == 404
        }
    }

    @Test
    void testExecuteNonExistent() {
        try {
            RestExecutor.execute(GET, "http://not.existig.host/not/existing/path".toURL())
        } catch (RestException re) {
            assert re.httpStatusCode == -1
        }
    }

    @Test
    void testPostTemplate() {
        def rest = new RestExecutor(bindings)
        def url = "http://localhost:$PORT" + '/successful/post?a=$aa&b=$bb'
        def payload = '{"a" : "$aa", "b" : "$bb"}'

        // now we can execute a template
        def result = rest.executeTemplate(POST, url, payload, TEXT)
        assert result == "success!"
    }

    @AfterClass
    static void shutDown() {
        wireMockServer.stop()
    }

}
