package kic.kafka.pipeliet.bolts.dto.grafana;

public class Search {
    private String target;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "Search{" +
                "target='" + target + '\'' +
                '}';
    }
}
