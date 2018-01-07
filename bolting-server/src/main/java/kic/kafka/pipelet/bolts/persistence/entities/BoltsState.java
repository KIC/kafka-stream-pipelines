package kic.kafka.pipelet.bolts.persistence.entities;

import kic.kafka.pipelet.bolts.persistence.keys.BoltsStateKey;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.nio.charset.Charset;
import java.util.Arrays;

@Entity
public class BoltsState {
    private static final Charset CHAR_SET = Charset.forName("UTF-8");

    @EmbeddedId
    private BoltsStateKey id;
    private long consumerOffset;
    private byte[] state = new byte[0];

    protected BoltsState() { }

    public BoltsState(BoltsStateKey id) {
        this.id = id;
        this.consumerOffset = -1;
    }

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
        this.state = state != null ? state : new byte[0];
    }


    public String stateAsString() {
        return new String(state, CHAR_SET);
    }

    public long nextConsumerOffset() {
        return getConsumerOffset() + 1;
    }

    public BoltsState withNewState(byte[] newState, long offset) {
        BoltsState newBoltState = new BoltsState(this.id);
        newBoltState.setState(newState);
        newBoltState.setConsumerOffset(offset);
        return newBoltState;
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
        return stateAsString();
    }
}
