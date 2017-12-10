package kic.kafka.pipeliet.bolts.services.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class LambdaTask<S, E> {
    private static final Logger log = LoggerFactory.getLogger(LambdaTask.class);
    private final BiFunction<S, E, S> lambda;
    private final Consumer<S> stateUpdate;
    private S state;

    public LambdaTask(Supplier<S> initialStateProvider,
                      BiFunction<S, E, S> lambda,
                      Consumer<S> stateUpdate
    ) {
        this.lambda = lambda;
        this.stateUpdate = stateUpdate;

        // use the initial state provider to read the sate from the data source (i.e. base)
        this.state = initialStateProvider.get();
    }

    public S execute(E event) throws LambdaException {
        try {
            // apply the event on the current state
            final S newState = lambda.apply(state, event);

            // store the new state in the database
            stateUpdate.accept(newState);

            // fianlly update the internal state
            return this.state = newState;
        } catch (Exception e) {
            throw new LambdaException(e);
        }
    }

    public S getCurrentState() {
        return state;
    }

}
