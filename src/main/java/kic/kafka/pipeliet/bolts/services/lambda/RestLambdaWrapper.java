package kic.kafka.pipeliet.bolts.services.lambda;

import kic.kafka.pipeliet.bolts.persistence.entities.BoltsState;
import kic.lambda.dispatch.RestLambda;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.function.BiFunction;

public class RestLambdaWrapper implements BiFunction<BoltsState, ConsumerRecord, BoltsState> {
    private final RestLambda restLambda;

    public RestLambdaWrapper(RestLambda restLambda) {
        this.restLambda = restLambda;
    }

    @Override
    public BoltsState apply(BoltsState boltsState, ConsumerRecord consumerRecord) {
        byte[] newState = restLambda.apply(new String(boltsState.getState()), consumerRecord).getBytes();
        return boltsState.withNewState(newState, consumerRecord.offset());
    }
}
