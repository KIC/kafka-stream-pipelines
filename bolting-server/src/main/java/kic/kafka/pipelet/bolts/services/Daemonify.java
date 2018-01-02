package kic.kafka.pipelet.bolts.services;

import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class Daemonify {

    public Daemonify() {
    }

    public Thread function(
            final Function<Long, Boolean> threadedFunction,
            final long sleepBetweenInvocations
    ) {
        return function(threadedFunction, sleepBetweenInvocations, e -> {throw new RuntimeException(e);});
    }

    public Thread function(
            final Function<Long, Boolean> threadedFunction,
            final long sleepBetweenInvocations,
            final Consumer<Exception> panicHandler
    ) {
        Thread thread = new Thread() {
            private long counter = 0;

            @Override
            public void run() {
                try {
                    while (threadedFunction.apply(counter++)) {
                        try {
                            Thread.sleep(sleepBetweenInvocations);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                } catch (Exception e) {
                    panicHandler.accept(e);
                }
            }
        };

        thread.setDaemon(true);
        return thread;
    }
}
