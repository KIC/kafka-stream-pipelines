package kic.kafka.pipelet.bolts.services.lambda;

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState;
import kic.kafka.pipelet.bolts.persistence.keys.BoltsStateKey;
import kic.kafka.pipelet.bolts.services.KafkaClientService;
import kic.kafka.pipelet.bolts.persistence.repositories.BoltsStateRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * we need to have two threadpools and two queues. one for the regular lambda calls and one retry pool
 * we use a priority queue in the retry case as we lower the priority after each execution failure
 *
 * in the regular task queue the executor takes one task from the queue pulls on the topic and
 *  on data:
 *    executes the callable and
 *      on success:
 *        forwards the result to the next topic
 *          on success:
 *            adds the task back to the task queue
 *          failure:
 *            we stored the new state but we can not send it forward we can put it into the retry
 *            queue but as a different type where no event needs to be paassed (and applied) anymore
 *      failure:
 *        the state only updated if the lambda and the persistence went well. thus it is completely
 *        safe to just put this task inclusie the lst pulled event into the retry queue.
 *          => NOTE we need a dedicated state object where we store the last successful offset!
 *  no data:
 *    put the task back into the queue
 *
 *
 *
 * in the retry queue the logic is slightly different.
 *
 * the executor takes the task with the highest priority from the queue and executes it.
 * there are two kinds of retry tasks. wether we need to execute the labda function and forward
 * or we just need to forward
 *  on lambda:
 *    execute the callable
 *      on success:
 *        forward the result to the next topic
 *          on success:
 *            add this task back to the regular task queue
 *          on failure:
 *            add this task back to the retry queue yet just the forwarding is missing
 *      on failure:
 *        add this task back to the retry queue yet with a lower priority
 *  on forward:
 *    execute the callable
 *      on success:
 *        add this task back to the regular task queue
 *      on failure:
 *        add this task back to the retry queue yet with a lower priority
 */
@Service
public class BoltingService extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(BoltingService.class);
    private static final long SLEEP_FOR_NEW_TASKS = 1000L;
    private final Queue<Task> taskQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Task> retryQueue = new PriorityBlockingQueue<>();
    private final BiFunction<String, String, Function<Long, List<ConsumerRecord>>> newTopicConsumer;
    private final Function<String, Consumer<Map.Entry<Object, byte[]>>> newTopicProvider;
    private final Function<BoltsStateKey, BoltsState> stateLoader;
    private final Consumer<BoltsState> stateUpdater;
    private final ExecutorService excutorService;
    private boolean running = false;

    /**
     * How do we handle the restart situation?
     * we can read the latest states from the database and create a lambdatask for each.
     * TODO how do we handle the cases where we have a successful state but we were not able to forward the new state
     * eventually we have a callback where we can upadate a state if the forward was successful or not
     *
     * LATER we also need to find a way how we want to deal with data inconsitency. if a state got lost or a topic or
     * the offsets can not be found or dont match. but with this kind of problems we can deal later
     */

    @Autowired
    public BoltingService(KafkaClientService kafkaClientService, BoltsStateRepository repository) {
        this(kafkaClientService::newTopicConsumer,
             kafkaClientService::newTopicProvider,
             repository::findOne,
             repository::save,
             Executors.newFixedThreadPool(1));
    }

    public BoltingService(BiFunction<String, String, Function<Long, List<ConsumerRecord>>> newTopicConsumer,
                          Function<String, Consumer<Map.Entry<Object, byte[]>>> newTopicProvider,
                          Function<BoltsStateKey, BoltsState> stateLoader,
                          Consumer<BoltsState> stateUpdater,
                          ExecutorService executorService
    ) {
        this.newTopicConsumer = newTopicConsumer;
        this.newTopicProvider = newTopicProvider;
        this.stateLoader = stateLoader;
        this.stateUpdater = stateUpdater;
        this.excutorService = executorService;
    }

    /**
     * we add a new lambda by creating a lambdatask and then we put it into the regular task queue.
     * we need also a callback function where we add this task into the database first. there we also need to
     * deal with the fact if this topic might already exists and eventually need to throw an exception
     *
     * send lambda as new RestLambdaWrapper(new RestLambda("http://..."));
     */
    public void add(String pipelineId, String serviceId, String sourceTopic, String targetTopic, BiFunction<BoltsState, ConsumerRecord, BoltsState> lambda) {
        final BoltsStateKey id = new BoltsStateKey(sourceTopic, targetTopic, serviceId); // FIXME is url needed for unique key?

        // FIXME / TODO persist this whole stuff into a database, on duplicate key throw exception (later update topic versions and reset offsets)
        LambdaTask<BoltsState, ConsumerRecord> task = new LambdaTask(() -> stateLoader.apply(id), new BoltsState(id), lambda, stateUpdater);
        Function<Long, List<ConsumerRecord>> pullForTopic = newTopicConsumer.apply(pipelineId, sourceTopic);
        Consumer<Map.Entry<Object, byte[]>> pushToTopic = newTopicProvider.apply(targetTopic);
        LambdaTaskExecutor lte = new LambdaTaskExecutor(task, pullForTopic, pushToTopic);

        // finally put lambda task executor into the task queue
        taskQueue.add(lte);
    }

    @Override
    public void start() {
        // inject this into the main commandline runner and start the service
        // todo we also want to resume everything where we left
        LOG.info("starting lambda executor service");
        running = true;
        super.start();
    }

    public void shutdown() {
        // inject this into the main commandline runner as shutdown hook and stop the service from there
        LOG.warn("stopping lambda executor service");
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Task task;
                while ((task = taskQueue.poll()) != null) {
                    LOG.debug("executing task: {}", task);
                    excutorService.submit(makeExecutor(task));
                }

                Thread.sleep(SLEEP_FOR_NEW_TASKS);
            } catch (Exception e) {
                shutdown();
                throw new RuntimeException(e);
            }
        }
    }

    private Runnable makeExecutor(final Task task) {
        return () -> {
            if (LOG.isDebugEnabled()) LOG.debug("execute tast {}", task);
            try {
                task.call();
                if (LOG.isDebugEnabled()) LOG.debug("task {} execution success, put back into queue", task);
                taskQueue.add(task);
            } catch (Exception e) {
                // whatever it is we need to retry the whole lot
                // FIXME what if just the sending went wrong?
                // FIXME provide error reason to the task
                LOG.warn("task execution failed, put it into the retry queue", e);
                retryQueue.add(task);
            }
        };
    }

    public List<Task> getActiveTasks() {
        return new ArrayList<>(taskQueue);
    }

    public List<Task> getFailingTasks() {
        return new ArrayList<>(retryQueue);
    }
}
