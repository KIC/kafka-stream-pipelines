package kic.kafka.pipelet.bolts.services.lambda;

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class LambdaTask implements Task {
    private static final Logger LOG = LoggerFactory.getLogger(LambdaTask.class);
    private final String taskId;
    private final Lambda<BoltsState, ConsumerRecord> lambda;
    private final Function<Long, List<ConsumerRecord>> eventSource;
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
                      Consumer<Task> successHandler,
                      Consumer<Task> failureHandler
    ) {
        this.taskId = taskId;
        this.lambda = lambda;
        this.eventSource = eventSource;
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
                // if in one cycle someting goes wrong then we can safly throw an excption.
                // the lambdatask takes care on the state update as well as forwarding it to he next topic
                // the caller of this callable takes care of the retry mechanism
                BoltsState newState = lambda.execute(event);
                lastKey = "" + event.key();
                lastValue = newState.stateAsString();
            }

            lastException = null;
            successHandler.accept(this);

            if (LOG.isDebugEnabled()) LOG.debug("task {} execution success", this);
            return null;
        } catch (Exception e) {
            LOG.warn("task execution failed {}\n{}", this, e);
            lastException = e;
            failureHandler.accept(this);
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
