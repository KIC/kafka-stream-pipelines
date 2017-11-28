package kic.kafka.pipeliet.bolts.dto.grafana;

public class Raw {
    private String from;
    private String to;

    public void setFrom(String from) {
         this.from = from;
     }

    public String getFrom() {
         return from;
     }

    public void setTo(String to) {
         this.to = to;
     }

    public String getTo() {
         return to;
     }

    @Override
    public String toString() {
        return "Raw{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}