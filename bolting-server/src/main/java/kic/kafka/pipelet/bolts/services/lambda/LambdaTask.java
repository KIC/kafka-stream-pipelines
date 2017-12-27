package kic.kafka.pipelet.bolts.services.lambda;

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaTask implements Task {
    private final String taskId;
    private final Lambda<BoltsState, ConsumerRecord> lambda;
    private final Function<Long, List<ConsumerRecord>> eventSource;
    private final Consumer<Map.Entry<String, String>> eventTarget;
    private Exception lastException = null;
    private String lastKey = null;
    private String lastValue = null;
    private int executionCount = 0;
    private boolean executing = false;

    public LambdaTask(String taskId,
                      Lambda<BoltsState, ConsumerRecord> lambda,
                      Function<Long, List<ConsumerRecord>> eventSource,
                      Consumer<Map.Entry<String, String>> eventTarget
    ) {
        this.taskId = taskId;
        this.lambda = lambda;
        this.eventSource = eventSource;
        this.eventTarget = eventTarget;
    }

    @Override
    public Void call() throws Exception {
        try {
            executing = true;
            List<ConsumerRecord> events = eventSource.apply(lambda.getCurrentState()
                                                                  .getConsumerOffset());

            for (ConsumerRecord<?, ?> event : events) {
                // if in one cycle someting goes wrong then we can safly throw an excption.
                // the lambdatask takes care on the state update and the caller of this callable
                // takes care of the retry mechanism
                BoltsState newState = lambda.execute(event);
                lastKey = "" + event.key();
                lastValue = newState.stateAsString();
                eventTarget.accept(new AbstractMap.SimpleImmutableEntry<>(lastKey, lastValue));
            }

            lastException = null;
            return null;
        } catch (Exception e) {
            lastException = e;
            throw e;
        } finally {
            executing = false;
            executionCount++;
        }
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public Exception getExcpetion() {
        return lastException;
    }

    @Override
    public String getLastResult() {
        return lastKey + ":" + lastValue;
    }

    @Override
    public int getExecutionCount() {
        return executionCount;
    }

    @Override
    public boolean isExecuting() {
        return executing;
    }

}
