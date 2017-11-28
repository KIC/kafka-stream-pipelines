package kic.kafka.pipeliet.bolts.dto.grafana;

public class Ping {
    private String status;

    public Ping() {
    }

    public Ping(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Ping{" +
                "status='" + status + '\'' +
                '}';
    }
}
