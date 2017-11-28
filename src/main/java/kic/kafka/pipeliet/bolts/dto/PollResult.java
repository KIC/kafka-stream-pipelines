package kic.kafka.pipeliet.bolts.dto;

import java.util.Map;

public class PollResult {
    private final boolean success;
    private long offset = -1L;
    private Map result = null;
    private String error;

    public PollResult(long offset, Map result) {
        this.offset = offset;
        this.result = result;
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

    public Map getResult() {
        return result;
    }

    public void setResult(Map result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
