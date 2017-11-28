package kic.kafka.pipeliet.bolts.dto.grafana;

import java.util.Date;

public class Range {
    private Date from;
    private Date to;
    private Raw raw;

    public void setFrom(Date from) {
         this.from = from;
     }

    public Date getFrom() {
         return from;
     }

    public void setTo(Date to) {
         this.to = to;
     }

    public Date getTo() {
         return to;
     }

    public void setRaw(Raw raw) {
         this.raw = raw;
     }

    public Raw getRaw() {
         return raw;
    }

    @Override
    public String toString() {
        return "Range{" +
                "from=" + from +
                ", to=" + to +
                ", raw=" + raw +
                '}';
    }
}