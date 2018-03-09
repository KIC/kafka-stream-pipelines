package kic.pipeline.sources.task

import com.fasterxml.jackson.databind.ObjectMapper
import kic.pipeline.sources.dto.Jobs
import spock.lang.Specification

import java.util.function.BiConsumer

class ShellTaskTest extends Specification {
    static final ObjectMapper JSON = new ObjectMapper();

    def "test execute"() {
        given:
        def result = [:]
        def jobspecs = JSON.readValue(ClassLoader.getSystemResourceAsStream("jobs.json"), Jobs.class)
        def job = jobspecs.jobs.find {it.id == "test-1"}
        def consumer = {k,v -> result[(k)] = v} as BiConsumer
        def task = new ShellTask(job.id, job.encoding, "", new File("."), job.command, job.keyExtractor, job.valueExtractor, consumer, null, null)

        when:
        task.execute(null)
        println(result)

        then:
        result == ["Hello World": "22",
                   "lala": "44"]

        // FIXME add negative test cases like
    }
}
