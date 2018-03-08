package kic.pipeline.sources.task

import spock.lang.Specification

class SimpleProcessTest extends Specification {
    def "test execute"() {
        given:
        def sp = new SimpleProcess(["bash", "-c", "cat"] as String[])

        when:
        def rs = sp.execute("hello world".getBytes("UTF-8"))

        println(rs.returnCode)
        println(new String(rs.stdOut, "UTF-8"));
        println(new String(rs.stdErr, "UTF-8"));

        then:
        rs.returnCode == 0
        new String(rs.stdOut, "UTF-8") == "hello world"
    }
}
