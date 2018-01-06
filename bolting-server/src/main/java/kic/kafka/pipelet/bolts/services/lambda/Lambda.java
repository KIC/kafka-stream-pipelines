package kic.kafka.pipelet.bolts.services.lambda;

import kic.kafka.pipelet.bolts.util.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Supplier;


public class Lambda<S, E> {
    private static final Logger log = LoggerFactory.getLogger(Lambda.class);
    private final BiFunction<S, E, S> lambda;
    private final Supplier<S> stateProvider;

    public Lambda(Supplier<S> stateProvider, BiFunction<S, E, S> lambda) {
        this.lambda = lambda;
        this.stateProvider = stateProvider;
    }

    public S execute(E event) throws LambdaException {
        try {
            // apply the event on the current state
            final S state = getCurrentState();
            final S newState = lambda.apply(state, event);

            // fianlly update the internal state
            Try.ignore(log.isDebugEnabled(), () -> log.debug("before: {}, event: {}, after: {}", state, event, newState));
            return newState;
        } catch (Exception e) {
            throw new LambdaException(e);
        }
    }

    public S getCurrentState() {
        return stateProvider.get();
    }

}
