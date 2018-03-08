package kic.pipeline.sources.task

import com.fasterxml.jackson.databind.ObjectMapper
import kic.pipeline.sources.dto.Jobs
import spock.lang.Specification

import java.util.function.BiConsumer

class ShellTaskTest extends Specification {
    static final ObjectMapper JSON = new ObjectMapper();

    def "test execute"() {
        given:
        def jobspecs = JSON.readValue(ClassLoader.getSystemResourceAsStream("jobs.json"), Jobs.class)
        def job = jobspecs.jobs.find {it.id == "test-1"}
        def consumer = {k,v -> println "$k: $v"} as BiConsumer
        def task = new ShellTask(job.id, job.encoding, "", new File("."), job.command, job.keyExtractor, job.valueExtractor, consumer)

        when:
        task.execute(null)

        then:
        1 == 1
    }
}
