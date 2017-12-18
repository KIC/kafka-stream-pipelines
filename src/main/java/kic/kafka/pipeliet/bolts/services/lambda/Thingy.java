package kic.kafka.pipeliet.bolts.services.lambda;

import kic.kafka.pipeliet.bolts.persistence.entities.BoltsState;
import kic.kafka.pipeliet.bolts.persistence.keys.BoltsStateKey;
import kic.kafka.pipeliet.bolts.persistence.repositories.BoltsStateRepository;
import kic.kafka.pipeliet.bolts.services.KafkaClientService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
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
public class Thingy extends Thread {
    private static final Logger log = LoggerFactory.getLogger(Thingy.class);
    private final Queue<LambdaTaskExecutor> taskQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Callable> retryQueue = new PriorityBlockingQueue<>();
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
    public Thingy(KafkaClientService kafkaClientService, BoltsStateRepository repository) {
        this(kafkaClientService::newTopicConsumer,
             kafkaClientService::newTopicProvider,
             repository::findOne,
             repository::save,
             Executors.newFixedThreadPool(1));
    }

    public Thingy(BiFunction<String, String, Function<Long, List<ConsumerRecord>>> newTopicConsumer,
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
        LambdaTask<BoltsState, ConsumerRecord> task = new LambdaTask(() -> stateLoader.apply(id), lambda, stateUpdater);
        Function<Long, List<ConsumerRecord>> pullForTopic = newTopicConsumer.apply(pipelineId, sourceTopic);
        Consumer<Map.Entry<Object, byte[]>> pushToTopic = newTopicProvider.apply(targetTopic);
        LambdaTaskExecutor lte = new LambdaTaskExecutor(task, pullForTopic, pushToTopic);

        // finally put lambda task executor into the task queue
        taskQueue.add(lte);
    }

    @Override
    public void start() {
        // inject this into the main commandline runner and start the service
        log.info("starting lambda executor service");
        running = true;
        super.start();
    }

    public void shutdown() {
        // inject this into the main commandline runner as shutdown hook and stop the service from there
        log.warn("stopping lambda executor service");
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                LambdaTaskExecutor task;
                while ((task = taskQueue.poll()) != null) {
                    excutorService.submit(makeExecutor(task));
                }

                Thread.sleep(100L);
            } catch (Exception e) {
                shutdown();
                throw new RuntimeException(e);
            }
        }
    }

    private Runnable makeExecutor(final LambdaTaskExecutor task) {
        return () -> {
            try {
                task.call();
                taskQueue.add(task);
            } catch (Exception e) {
                // whatever it is we need to retry the whole lot
                // FIXME what if just the sending went wrong?
                retryQueue.add(task);
            }
        };
    }
}