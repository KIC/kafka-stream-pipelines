@Grab(group='com.sparkjava', module='spark-core', version='2.7.1')
import static spark.Spark.*
@Grab(group='com.sparkjava', module='spark-core', version='2.7.1')
import static spark.Spark.*

println('''
Usage:
curl -H "Content-Type: text/plain" \\
-X POST 'http://localhost:8080/api/v1/bolt/demo-pipeline/demo-service-1/demo.returns/demo.performance/GET' \\
-d 'http://localhost:4567/demo/fold?key=${event.key}&value=${event.value}&state=${state}&offset=${state.nextConsumerOffset()}'
''')

get("/demo/fold", {req, res ->
    def key = req.queryParams("key")
    def value = req.queryParams("value").toDouble()
    def state = req.queryParams("state") ? req.queryParams("state").toDouble() : 1d
    def offset = req.queryParams("offset")
    def newState = state * (1d + value)

    println("$key offeset: $offset, fold last value $state * (1d + $value) = $newState")
    return newState.toString()
})

