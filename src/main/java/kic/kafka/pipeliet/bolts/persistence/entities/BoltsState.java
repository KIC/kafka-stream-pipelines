package kic.kafka.pipeliet.bolts.persistence.entities;

import kic.kafka.pipeliet.bolts.persistence.keys.BoltsStateKey;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.util.Arrays;

@Entity
public class BoltsState {
    @EmbeddedId
    private BoltsStateKey id;
    private long consumerOffset;
    private byte[] state;

    public BoltsStateKey getId() {
        return id;
    }

    public void setId(BoltsStateKey id) {
        this.id = id;
    }

    public long getConsumerOffset() {
        return consumerOffset;
    }

    public void setConsumerOffset(long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    public byte[] getState() {
        return state;
    }

    public void setState(byte[] state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "BoltsState{" +
                "id=" + id +
                ", consumerOffset=" + consumerOffset +
                ", state=" + Arrays.toString(state) +
                '}';
    }
}
