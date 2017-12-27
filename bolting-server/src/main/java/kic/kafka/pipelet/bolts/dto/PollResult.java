package kic.kafka.pipelet.bolts.dto;

import java.util.List;

public class PollResult {
    private final boolean success;
    private List<Long> offsets;
    private List keys = null;
    private List values = null;
    private String error;

    public PollResult(List<Long> offsets, List keys, List values) {
        if (keys.size() != values.size() && keys.size() == offsets.size()) throw new IllegalArgumentException("keys and values not of same size");
        this.offsets = offsets;
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

    public List<Long> getOffsets() {
        return offsets;
    }

    public List getKeys() {
        return keys;
    }

    public List getValues() {
        return values;
    }

    public String getError() {
        return error;
    }

}
