package kic.pipeline.sources.spring.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.ZonedDateTime;

@Entity
@Table(name = "job_state")
public class JobState {
    @Id private String jobId;
    @Version private Long version;
    @Lob private String stdOut;
    @Lob private String stdErr;
    @Column(name="last_key")
    private String key;
    @Column(name="last_value")
    private String value;
    private Long createdAt;

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

    @PrePersist
    @PreUpdate
    void addTimestamp() {
        createdAt = ZonedDateTime.now().toEpochSecond();
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

    @Override
    public String toString() {
        return "JobState{" +
                "jobId='" + jobId + '\'' +
                ", stdOut='" + stdOut + '\'' +
                ", stdErr='" + stdErr + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
