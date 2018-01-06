package kic.kafka.pipelet.bolts.services.lambda

import kic.kafka.pipelet.bolts.util.Try
import spock.lang.Specification

import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

class ResendTaskTest extends Specification {
    def taskId = "Test resend"

    def "test call keep failing"() {
        given:
        def ok = false
        def nok = String.class
        def pushed = false

        def lambda = new Lambda({-> 1} as Supplier, {s, e -> 1} as BiFunction)
        def succes = {t -> ok = true} as Consumer
        def failed = {t -> nok = t.class} as Consumer
        def push = {e -> throw new RuntimeException("no push!")} as Consumer
        def lamdaTask = new LambdaTask(taskId, lambda, { o -> []} as Function , { s ->} as Consumer, push, succes, failed)

        when:
        Try.ignore({ -> new ResendTask(lamdaTask).call()})

        then:
        ok == false
        nok == ResendTask.class
        pushed == false
        lamdaTask.lastKey == null
        lamdaTask.lastValue == null
    }

    def "test call recover"() {
        given:
        def ok = false
        def nok = String.class
        def pushed = false

        def lambda = new Lambda({-> 1} as Supplier, {s, e -> 1} as BiFunction)
        def succes = {t -> ok = true} as Consumer
        def failed = {t -> nok = t.class} as Consumer
        def push = {e -> pushed = true} as Consumer
        def lamdaTask = new LambdaTask(taskId, lambda, { o -> []} as Function , { s ->} as Consumer, push, succes, failed)

        when:
        Try.ignore({ -> new ResendTask(lamdaTask).call()})

        then:
        ok == true
        nok == String.class
        pushed == true
    }
}
