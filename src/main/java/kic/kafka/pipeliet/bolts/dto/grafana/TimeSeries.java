package kic.kafka.pipeliet.bolts.dto.grafana;

import java.util.List;

public class TimeSeries {
    private String target;
    private List<Number[]> datapoints;

    public TimeSeries() {
    }

    public TimeSeries(String target, List<Number[]> datapoints) {
        this.target = target;
        this.datapoints = datapoints;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<Number[]> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<Number[]> datapoints) {
        this.datapoints = datapoints;
    }

    @Override
    public String toString() {
        return "TimeSeries{" +
                "target='" + target + '\'' +
                ", datapoints=" + datapoints +
                '}';
    }
}
