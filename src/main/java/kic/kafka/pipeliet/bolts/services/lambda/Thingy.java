package kic.kafka.pipeliet.bolts.services.lambda;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

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
public class Thingy {
    private final Queue<LambdaTaskExecutor> taskQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Object> retryQueue = new PriorityBlockingQueue<>();

    /**
     * How do we handle the restart situation?
     * we can read the latest states from the database and create a lambdatask for each.
     * TODO how do we handle the cases where we have a successful state but we were not able to forward the new state
     * eventually we have a callback where we can upadate a state if the forward was successful or not
     *
     * LATER we also need to find a way how we want to deal with data inconsitency. if a state got lost or a topic or
     * the offsets can not be found or dont match. but with this kind of problems we can deal later
     */
    public Thingy() {
    }

    /**
     * we add a new lambda by creating a lambdatask and then we put it into the regular task queue.
     * we need also a callback function where we add this task into the database first. there we also need to
     * deal with the fact if this topic already exists and eventually throw an exception
     */
    public void add() {

    }
}
