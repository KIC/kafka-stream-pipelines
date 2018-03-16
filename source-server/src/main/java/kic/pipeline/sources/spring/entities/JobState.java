package kic.pipeline.sources.spring.entities;

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
    @Lob private String errMsg;
    private Long createdAt;

    protected JobState() {}

    public JobState(String jobId) {
        this.jobId = jobId;
    }

    public JobState(String jobId, String stdOut, String stdErr, String errMsg) {
        this.jobId = jobId;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.errMsg = errMsg;
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

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }


    @Override
    public String toString() {
        return "JobState{" +
                "jobId='" + jobId + '\'' +
                ", stdOut='" + stdOut + '\'' +
                ", stdErr='" + stdErr + '\'' +
                ", errMsg='" + errMsg + '\'' +
                '}';
    }
}
