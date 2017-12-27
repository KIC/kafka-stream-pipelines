package kic.kafka.pipelet.bolts.dto;

import kic.kafka.pipelet.bolts.services.lambda.Task;
import org.apache.commons.lang.exception.ExceptionUtils;

public class TaskDescription {
    private String task;
    private String lastException;
    private String lastResult;
    private boolean executing;
    private int executionCount;

    public TaskDescription(Task task) {
        this.task = task.getTaskId();
        this.lastResult = task.getLastResult();
        this.executing = task.isExecuting();
        this.executionCount = task.getExecutionCount();
        this.lastException = task.getExcpetion() != null
            ? ExceptionUtils.getStackTrace(task.getExcpetion())
            : "";
    }

    public String getTask() {
        return task;
    }

    public String getLastException() {
        return lastException;
    }

    public String getLastResult() {
        return lastResult;
    }

    public boolean isExecuting() {
        return executing;
    }

    public int getExecutionCount() {
        return executionCount;
    }

}
