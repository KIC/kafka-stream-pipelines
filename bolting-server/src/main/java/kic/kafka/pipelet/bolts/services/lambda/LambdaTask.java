package kic.kafka.pipelet.bolts.services.lambda;

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState;
import kic.kafka.pipelet.bolts.util.Try;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaTask implements Task {
    private static final Logger LOG = LoggerFactory.getLogger(LambdaTask.class);
    private final String taskId;
    private final Lambda<BoltsState, ConsumerRecord> lambda;
    private final Function<Long, List<ConsumerRecord>> eventSource;
    private final Consumer<BoltsState> stateUpdater;
    private final Consumer<Map.Entry<String, String>> eventTarget;
    private final Consumer<Task> successHandler;
    private final Consumer<Task> failureHandler;
    private Exception lastException = null;
    private String lastKey = null;
    private String lastValue = null;
    private int executionCount = 0;
    private boolean executing = false;

    public LambdaTask(String taskId,
                      Lambda<BoltsState, ConsumerRecord> lambda,
                      Function<Long, List<ConsumerRecord>> eventSource,
                      Consumer<BoltsState> stateUpdater,
                      Consumer<Map.Entry<String, String>> eventTarget,
                      Consumer<Task> successHandler,
                      Consumer<Task> failureHandler
    ) {
        this.taskId = taskId;
        this.lambda = lambda;
        this.eventSource = eventSource;
        this.stateUpdater = stateUpdater;
        this.eventTarget = eventTarget;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Override
    public Void call() throws Exception {
        if (LOG.isDebugEnabled()) LOG.debug("execute tast {}", this);

        try {
            executing = true;
            List<ConsumerRecord> events = eventSource.apply(lambda.getCurrentState()
                    .nextConsumerOffset());

            for (ConsumerRecord<?, ?> event : events) {
                BoltsState newState = lambda.execute(event);

                // try to save the state and forward the result
                stateUpdater.accept(newState);
                lastKey = "" + event.key();
                lastValue = newState.stateAsString();
                lastException = null;

                try {
                    pushForward();
                } catch (Exception e) {
                    failureHandler.accept(new ResendTask(this));
                    return null;
                }
            }

            succeeded();
            return null;
        } catch (Exception e) {
            if (!(e.getCause() instanceof URISyntaxException)) {
                fail(this, e);
            }
            throw e;
        } finally {
            executing = false;
            executionCount++;
        }
    }


    protected void pushForward() {
        eventTarget.accept(new AbstractMap.SimpleImmutableEntry<>(lastKey, lastValue));
    }

    protected void succeeded() {
        // the success handler puts the task back to the task queue
        Try.ignore(LOG.isDebugEnabled(), () -> LOG.debug("task execution success: {} \n{}\n", this, lastValue));
        successHandler.accept(this);
    }

    protected void fail(Task task, Exception e) {
        // failure handler puts the task abck to the retry queue
        Try.ignore(() -> LOG.warn("task execution failed {}\n{}\n{}\n", ExceptionUtils.getStackTrace(e), task, lastKey, lastValue));
        lastException = e;
        failureHandler.accept(task);
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

    @Override
    public String toString() {
        return "LambdaTask{" +
                "taskId='" + taskId + '\'' +
                ", lastException=" + lastException +
                ", lastKey='" + lastKey + '\'' +
                ", executionCount=" + executionCount +
                '}';
    }

}
