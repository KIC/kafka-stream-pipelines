package kic.kafka.pipelet.bolts.services.lambda;

import kic.kafka.pipelet.bolts.persistence.entities.BoltsState;
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
        byte[] newState = restLambda.apply(boltsState.stateAsString(), consumerRecord).getBytes();
        return boltsState.withNewState(newState, consumerRecord.offset());
    }
}
