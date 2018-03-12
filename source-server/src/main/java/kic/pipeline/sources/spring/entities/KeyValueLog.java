package kic.pipeline.sources.spring.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.ZonedDateTime;

@Entity
@Table(name = "job_key_value_log", uniqueConstraints = @UniqueConstraint(columnNames={"job_id", "extracted_key"}))
public class KeyValueLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name="job_id")
    private String jobId;
    @Column(name="extracted_key")
    private String key;
    @Column(name="extracted_value")
    private String value;
    private Long createdAt;

    protected KeyValueLog() {}

    public KeyValueLog(String jobId, String key, String value) {
        this.jobId = jobId;
        this.key = key;
        this.value = value;
    }

    @PrePersist
    void addTimestamp() {
        createdAt = ZonedDateTime.now().toEpochSecond();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
