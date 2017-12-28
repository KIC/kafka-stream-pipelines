package kic.kafka.pipelet.bolts.dto;

import kic.kafka.simpleclient.Records;

import java.util.ArrayList;
import java.util.List;

public class PollResult {
    private final boolean success;
    private final long lastOffset;
    private List<Long> offsets;
    private List keys = null;
    private List values = null;
    private String error;

    public PollResult(List<Long> offsets, List keys, List values) {
        if (keys.size() != values.size() && keys.size() == offsets.size()) throw new IllegalArgumentException("keys and values not of same size");
        this.lastOffset = offsets.size() > 0 ? offsets.get(offsets.size() - 1) : 0;
        this.offsets = offsets;
        this.keys = keys;
        this.values = values;
        this.success = true;
    }

    public PollResult(String error) {
        this.lastOffset = -1;
        this.error = error;
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<Long> getOffsets() {
        return offsets;
    }

    public long getLastOffset() {
        return lastOffset;
    }

    public long getNextOffset() {
        return offsets.size() > 0 ? getLastOffset() + 1 : 0;
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

    public PollResult append(Records<String, String> extension) {
        if (offsets.size() <= 0) return new PollResult(extension.offsets(), extension.keys(), extension.values());

        int newSize = offsets.size() + extension.size();
        List<Long> newOffsets = new ArrayList<>(newSize);
        List newKeys = new ArrayList<>(newSize);
        List newValues = new ArrayList<>(newSize);

        newOffsets.addAll(offsets);
        newOffsets.addAll(extension.offsets());
        newKeys.addAll(keys);
        newKeys.addAll(extension.keys());
        newValues.addAll(values);
        newValues.addAll(extension.values());

        return new PollResult(newOffsets, newKeys, newValues);
    }

    public static PollResult emtpy() {
        return new PollResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
}
