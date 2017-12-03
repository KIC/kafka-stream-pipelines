package kic.kafka.pipeliet.bolts.services;

import kic.kafka.pipeliet.bolts.dto.Event;
import kic.lambda.dispatch.RestException;

import java.util.function.BiFunction;

public class LambdaService {

    // state_t = f(state_t-1, toString, lambda, fromString)
    public String calculateNewState(BiFunction<String, Event, String> lambda, String state, Event event) {
        try {
            // read state from database
            // apply event
            // store the new state in the database
            // invoke callback (which forwards the new state to the next topic)
            // QUESTION! Since we forward the new state 1:1 to a new topic,
            // we could skip the database at all and just query the follwoing topic
            // however we would ned to enrich the state somehow by offset information of the source tapoic no?
            String newState = lambda.apply(state, event);

        } catch (Exception e) {

        }
        return null;
    }
}
