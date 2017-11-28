import groovy.json.JsonOutput

@Grab(group='com.sparkjava', module='spark-core', version='2.7.1')
import static spark.Spark.*

get("/hello/:name", {req, res ->
    println("dasdas")
    return JsonOutput.toJson([lala:12])
})

