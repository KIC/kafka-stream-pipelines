package kic.kafka.pipelet.bolts.services.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResendTask implements Task {
    private static final Logger LOG = LoggerFactory.getLogger(ResendTask.class);
    private final LambdaTask lambdaTask;
    private boolean executing = false;
    private int retryCount = 0;

    public ResendTask(LambdaTask lambdaTask) {
        this.lambdaTask = lambdaTask;
    }

    @Override
    public Void call() throws Exception {
        // all we do is we retry to send the current state of the lambdaTask to the target topic
        // if this succeeds then we extract the lambdaTask and put it back to the regular queue
        // if if fails we keep adding back this task to the retry queue
        executing = true;

        try {
            lambdaTask.pushForward();
            lambdaTask.succeeded();
        } catch (Exception e) {
            lambdaTask.fail(this, e);
        } finally {
            retryCount++;
            executing = false;
        }

        return null;
    }

    @Override
    public String getTaskId() {
        return "resend(" + lambdaTask.getTaskId() + ")";
    }

    @Override
    public Exception getExcpetion() {
        return lambdaTask.getExcpetion();
    }

    @Override
    public String getLastResult() {
        return lambdaTask.getLastResult();
    }

    @Override
    public int getExecutionCount() {
        return retryCount;
    }

    @Override
    public boolean isExecuting() {
        return executing;
    }

}
