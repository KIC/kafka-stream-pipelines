package kic.kafka.pipelet.bolts.services.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class TaskExecuteFunction implements Function<Long, Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(TaskExecuteFunction.class);
    private final ExecutorService excutorService;
    private final Queue<Task> taskQueue;
    private final AtomicBoolean running;

    public TaskExecuteFunction(
            @NotNull ExecutorService excutorService,
            @NotNull Queue<Task> taskQueue,
            @NotNull AtomicBoolean running
    ) {
        this.excutorService = excutorService;
        this.taskQueue = taskQueue;
        this.running = running;
    }

    @Override
    public Boolean apply(Long count) {
        Task task;
        while ((task = taskQueue.poll()) != null) {
            LOG.debug("executing task: {}", task);
            excutorService.submit(task);
        }

        if (!running.get()) {
            excutorService.shutdown();
            return false;
        } else {
            return true;
        }
    }
}
