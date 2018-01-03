package kic.kafka.pipelet.bolts.services.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;


public class Lambda<S, E> {
    private static final Logger log = LoggerFactory.getLogger(Lambda.class);
    private final BiFunction<S, E, S> lambda;
    private final BiConsumer<E, S> stateUpdate;
    private S state;

    public Lambda(Supplier<S> initialStateProvider,
                  BiFunction<S, E, S> lambda,
                  BiConsumer<E, S> stateUpdate
    ) {
        this(initialStateProvider, null, lambda, stateUpdate);
    }

    public Lambda(Supplier<S> initialStateProvider,
                  S defaultState,
                  BiFunction<S, E, S> lambda,
                  BiConsumer<E, S> stateUpdate
    ) {
        this.lambda = lambda;
        this.stateUpdate = stateUpdate;

        // use the initial state provider to read the sate from the data source (i.e. base)
        this.state = coalesce(initialStateProvider.get(), defaultState);
    }

    public S execute(E event) throws LambdaException {
        try {
            // apply the event on the current state
            final S newState = lambda.apply(state, event);

            // store the new state in the database
            stateUpdate.accept(event, newState);

            // fianlly update the internal state
            if (log.isDebugEnabled()) log.debug("before: {}, event: {}, after: {}", state, event, newState);
            return this.state = newState;
        } catch (Exception e) {
            throw new LambdaException(e);
        }
    }

    public S getCurrentState() {
        return state;
    }

    private <T>T coalesce(T... args) {
        for (T arg : args) {
            if (arg != null) return arg;
        }

        return null;
    }
}
