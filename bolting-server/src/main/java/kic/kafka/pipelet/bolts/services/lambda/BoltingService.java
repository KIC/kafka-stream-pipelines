package kic.kafka.pipelet.bolts.services.lambda;

import groovyx.net.http.Method;
import kic.kafka.pipelet.bolts.persistence.entities.BoltsState;
import kic.kafka.pipelet.bolts.persistence.keys.BoltsStateKey;
import kic.kafka.pipelet.bolts.persistence.repositories.BoltsStateRepository;
import kic.kafka.pipelet.bolts.services.Daemonify;
import kic.kafka.pipelet.bolts.services.KafkaClientService;
import kic.lambda.dispatch.RestLambda;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
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
public class BoltingService {
    private static final Logger LOG = LoggerFactory.getLogger(BoltingService.class);
    private static final long SLEEP_BETWEEN_NEW_TASKS = 1000L;
    private static final long SLEEP_BETWEEN_RETRIES = 5000L;

    private final Queue<Task> taskQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Task> retryQueue = new ConcurrentLinkedQueue<>();
    private final Set<Task> knownTasks = new HashSet<>();
    private final Consumer<Task> successHandler = task -> taskQueue.add(task);
    private final Consumer<Task> failureHandler = task -> retryQueue.add(task);
    private final BiFunction<String, String, Function<Long, List<ConsumerRecord>>> newTopicConsumer;
    private final Function<String, Consumer<Map.Entry<String, String>>> newTopicProvider;
    private final Function<BoltsStateKey, BoltsState> stateLoader;
    private final Consumer<BoltsState> stateUpdater;
    private final ExecutorService excutorService;
    private final Daemonify deamonifier;
    private AtomicBoolean running = new AtomicBoolean(false);

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
    public BoltingService(KafkaClientService kafkaClientService, BoltsStateRepository repository, Daemonify deamonifier) {
        this(kafkaClientService::newTopicConsumer,
             kafkaClientService::newTopicProvider,
             repository::findOne,
             repository::save,
             Executors.newFixedThreadPool(1), // FIXME inject a better threadpool
             deamonifier);
    }

    public BoltingService(BiFunction<String, String, Function<Long, List<ConsumerRecord>>> newTopicConsumer,
                          Function<String, Consumer<Map.Entry<String, String>>> newTopicProvider,
                          Function<BoltsStateKey, BoltsState> stateLoader,
                          Consumer<BoltsState> stateUpdater,
                          ExecutorService executorService,
                          Daemonify deamonifier
    ) {
        this.newTopicConsumer = newTopicConsumer;
        this.newTopicProvider = newTopicProvider;
        this.stateLoader = stateLoader;
        this.stateUpdater = stateUpdater;
        this.excutorService = executorService;
        this.deamonifier = deamonifier;
    }


    /**
     * we add a new lambda by creating a lambdatask and then we put it into the regular task queue.
     * we need also a callback function where we add this task into the database first. there we also need to
     * deal with the fact if this topic might already exists and eventually need to throw an exception
     *
     * send lambda as new RestLambdaWrapper(new RestLambda("http://..."));
     */
    public void add(BoltsStateKey boltsStateKey, String urlTemplate, String method, String payloadTemplate, String contentType) {
        // TODO add pipeline and service id and such as data to the groovy string template
        RestLambdaWrapper lambdaWrapper = new RestLambdaWrapper(new RestLambda(urlTemplate, Method.valueOf(method), payloadTemplate));

        try {
            // FIXME / TODO persist an initial empty state into a database
            stateUpdater.accept(new BoltsState(boltsStateKey));
            add(boltsStateKey, lambdaWrapper);
        } catch (Exception e) {
            // TODO on duplicate key throw exception (later update topic versions and reset offsets)
            // TODO we want versioned topics
            throw new RuntimeException("need to fix this!");
        }
    }

    private void add(BoltsStateKey boltsStateKey, BiFunction<BoltsState, ConsumerRecord, BoltsState> lambdaFunction) {
        final String taskId = boltsStateKey.toString();
        final BiConsumer<ConsumerRecord, BoltsState> updateAndForward = makeUpdateAndForwardFunction(boltsStateKey.getOutboundTopic());
        final Lambda<BoltsState, ConsumerRecord> lambda = new Lambda(() -> stateLoader.apply(boltsStateKey), new BoltsState(boltsStateKey), lambdaFunction, updateAndForward);
        final Function<Long, List<ConsumerRecord>> pullForTopic = newTopicConsumer.apply(taskId, boltsStateKey.getInboundTopic());
        final LambdaTask lte = new LambdaTask(taskId, lambda, pullForTopic, successHandler, failureHandler);

        // remember task
        knownTasks.add(lte);

        // finally put lambda task executor into the task queue
        taskQueue.add(lte);
    }

    private BiConsumer<ConsumerRecord, BoltsState> makeUpdateAndForwardFunction(String forTopic) {
        final Consumer<Map.Entry<String, String>> pushToTopic = newTopicProvider.apply(forTopic);

        // we need to put the kafka producer ${newTopicProvider.apply(targetTopic)} inside of the state updater
        // so we only update the state when we managed to push it forward
        return (event, newState) -> {
            pushToTopic.accept(new AbstractMap.SimpleImmutableEntry<>(event.key().toString(), newState.stateAsString()));
            stateUpdater.accept(newState);
        };
    }

    public void start() {
        // inject this into the main commandline runner and start the service
        // todo we also want to resume everything where we left after a server restart
        LOG.info("starting lambda executor service");
        running.set(true);

        deamonifier.function(new TaskExecuteFunction(excutorService, taskQueue, running), SLEEP_BETWEEN_NEW_TASKS).start();
        deamonifier.function(new TaskExecuteFunction(excutorService, retryQueue, running), SLEEP_BETWEEN_RETRIES).start();
    }

    public void shutdown() {
        // inject this into the main commandline runner as shutdown hook and stop the service from there
        LOG.warn("stopping lambda executor service");
        running.set(false);
    }

    public List<Task> getTasks() {
        return new ArrayList<>(knownTasks);
    }

}
