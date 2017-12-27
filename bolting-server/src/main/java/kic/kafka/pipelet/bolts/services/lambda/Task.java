package kic.kafka.pipelet.bolts.services.lambda;

import java.util.concurrent.Callable;

public interface Task extends Callable<Void> {

    String getTaskId();

    Exception getExcpetion();

    String getLastResult();

    int getExecutionCount();

    boolean isExecuting();
}
