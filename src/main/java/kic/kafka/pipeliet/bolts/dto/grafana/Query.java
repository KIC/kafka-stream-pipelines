package kic.kafka.pipeliet.bolts.dto.grafana;

import java.util.List;

public class Query {
    private int panelid;
    private Range range;
    private RangeRaw rangeRaw;
    private String interval;
    private int intervalMs;
    private List<Targets> targets;
    private String format;
    private int maxDataPoints;

    public void setPanelid(int panelid) {
         this.panelid = panelid;
     }

    public int getPanelid() {
         return panelid;
     }

    public void setRange(Range range) {
         this.range = range;
     }

    public Range getRange() {
         return range;
     }

    public void setRangeRaw(RangeRaw rangeRaw) {
         this.rangeRaw = rangeRaw;
     }

    public RangeRaw getRangeRaw() {
         return rangeRaw;
     }

    public void setInterval(String interval) {
         this.interval = interval;
     }

    public String getInterval() {
         return interval;
     }

    public void setIntervalMs(int intervalMs) {
         this.intervalMs = intervalMs;
     }

    public int getIntervalMs() {
         return intervalMs;
     }

    public void setTargets(List<Targets> targets) {
         this.targets = targets;
     }

    public List<Targets> getTargets() {
         return targets;
     }

    public void setFormat(String format) {
         this.format = format;
     }

    public String getFormat() {
         return format;
     }

    public void setMaxDataPoints(int maxDataPoints) {
         this.maxDataPoints = maxDataPoints;
     }

    public int getMaxDataPoints() {
         return maxDataPoints;
     }

    @Override
    public String toString() {
        return "Query{" +
                "panelid=" + panelid +
                ", range=" + range +
                ", rangeRaw=" + rangeRaw +
                ", interval='" + interval + '\'' +
                ", intervalMs=" + intervalMs +
                ", targets=" + targets +
                ", format='" + format + '\'' +
                ", maxDataPoints=" + maxDataPoints +
                '}';
    }
}
