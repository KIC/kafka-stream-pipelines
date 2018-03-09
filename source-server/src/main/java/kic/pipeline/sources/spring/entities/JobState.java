package kic.pipeline.sources.spring.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class JobState {
    @Id private String jobId;
    @Lob private String stdOut;
    @Lob private String stdErr;
    private String key;
    private String value;

    protected JobState() {}

    public JobState(String jobId) {
        this.jobId = jobId;
    }

    public JobState(String jobId, String stdOut, String stdErr, String key, String value) {
        this.jobId = jobId;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.key = key;
        this.value = value;
    }

    public String getJobId() {
        return jobId;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        if (key != null) this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (value != null) this.value = value;
    }
}
