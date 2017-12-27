package kic.kafka.pipeliet.bolts.services.lambda;

import kic.kafka.pipeliet.bolts.persistence.entities.BoltsState;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaTaskExecutor implements Task {
    private final LambdaTask<BoltsState, ConsumerRecord> lambdaTask;
    private final Function<Long, List<ConsumerRecord>> eventSource;
    private final Consumer<Map.Entry<Object, byte[]>> eventTarget;

    public LambdaTaskExecutor(LambdaTask<BoltsState, ConsumerRecord> lambdaTask,
                              Function<Long, List<ConsumerRecord>> eventSource,
                              Consumer<Map.Entry<Object, byte[]>> eventTarget
    ) {
        this.lambdaTask = lambdaTask;
        this.eventSource = eventSource;
        this.eventTarget = eventTarget;
    }

    @Override
    public Void call() throws Exception {
        List<ConsumerRecord> events = eventSource.apply(lambdaTask.getCurrentState()
                                                                  .getConsumerOffset());

        for (ConsumerRecord<?, ?> event : events) {
            // if in one cycle someting goes wrong then we can safly throw an excption.
            // the lambdatask takes care on the state update and the caller of this callable
            // takes care of the retry mechanism
            BoltsState newEvent = lambdaTask.execute(event);
            eventTarget.accept(new AbstractMap.SimpleImmutableEntry<>(event.key(), newEvent.getState()));
        }

        return null;
    }
}
