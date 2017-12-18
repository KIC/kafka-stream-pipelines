/*package kic.kafka.pipeliet.bolts.services.bolting;

import kic.lambda.dispatch.LambdaExecutor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static groovyx.net.http.Method.GET;
@Deprecated
public class PipeletExecutor implements Callable<LambdaStatus> {
    private static final long IMMEDIATLY_RETURN = 0;
    private final LambdaExecutor lambdaExecutor;
    private final Consumer<Long, String> kafaConsumer;
    private final Producer<Long, String> producer;
    private final Pipelet pipelet;

    // FIXME dont pass kafka kafkaConsumer and producer, instead pass labmdas so this class is testable
    public PipeletExecutor(LambdaExecutor lambdaExecutor, Consumer<Long, String> kafaConsumer, Producer<Long, String> producer, Pipelet pipelet) {
        this.lambdaExecutor = lambdaExecutor;
        this.kafaConsumer = kafaConsumer;
        this.producer = producer;
        this.pipelet = pipelet;

        initConsumer(0);
    }

    private void initConsumer(long offset) {
        // subscribers are lazy, you need to dummy poll before you seek!
        kafaConsumer.subscribe(Arrays.asList(pipelet.getSourceTopic()));
        kafaConsumer.poll(0);
        kafaConsumer.seek(new TopicPartition(pipelet.getSourceTopic(), 0), offset);
    }

    @Override
    public LambdaStatus call() throws Exception {
        // TODO if something went wrong we need to seek for the last success offset + 1
        ConsumerRecords<Long, String> records = kafaConsumer.poll(IMMEDIATLY_RETURN);
        for (ConsumerRecord<Long, String> record : records) {
            lambdaExecutor.execute(GET, pipelet.getState(), record.key(), record.value());

            String resultJson = ""; // rt.postForObject(pipelet.getLambda(), lambdaPayload, String.class);
            if (!resultJson.isEmpty()) {
                producer.send(pipelet.getTargetTopic(), record.key(), resultJson);
                // pipelet.updateState(resultJson); // FIXME now we would ned to convert the result to some object, groovy json slurper?
                // TODO if we have a valid result and the send was ok, then we commit to the database.
            } else {
                // TODO if we have an empty but valid result then we commit to the database.
            }
        }

        return LambdaStatus.OK;
    }

}
*/