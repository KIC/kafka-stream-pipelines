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


    public BoltsState withNewState(byte[] newState, long offset) {
        setState(newState);
        setConsumerOffset(offset);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoltsState that = (BoltsState) o;

        if (consumerOffset != that.consumerOffset) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return Arrays.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (consumerOffset ^ (consumerOffset >>> 32));
        result = 31 * result + Arrays.hashCode(state);
        return result;
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
