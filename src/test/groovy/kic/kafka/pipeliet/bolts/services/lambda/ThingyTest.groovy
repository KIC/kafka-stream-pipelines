package kic.kafka.pipeliet.bolts.services.lambda

import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ThingyTest extends Specification {
    static Thingy thingyService = new Thingy(null, null, null, null, currentThreadExecutorService()) // FIXME

    def "test add new lambda executing service service"() {
        given:
        def a;

        when:
        a = 1

        then:
        a == a
    }

    def setupSpec() {
        thingyService.start()
    }

    def cleanupSpec() {
        thingyService.shutdown()
    }

    // dummy executor service for testing
    private static ExecutorService currentThreadExecutorService() {
        ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy();
        return new ThreadPoolExecutor(0, 1, 0L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), callerRunsPolicy) {
            @Override void execute(Runnable command) {
                callerRunsPolicy.rejectedExecution(command, this);
            }
        };
    }
}
