package kic.kafka.pipelet.bolts.dto;

import java.util.List;

public class PollResult {
    private final boolean success;
    private long offset = -1L;
    private List keys = null;
    private List values = null;
    private String error;

    public PollResult(long offset, List keys, List values) {
        if (keys.size() != values.size()) throw new IllegalArgumentException("keys and values not of same size");
        this.offset = offset;
        this.keys = keys;
        this.values = values;
        this.success = true;
    }

    public PollResult(String error) {
        this.error = error;
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public List getKeys() {
        return keys;
    }

    public void setKeys(List keys) {
        this.keys = keys;
    }

    public List getValues() {
        return values;
    }

    public void setValues(List values) {
        this.values = values;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
