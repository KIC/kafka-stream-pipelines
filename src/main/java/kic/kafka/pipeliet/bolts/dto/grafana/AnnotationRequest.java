package kic.kafka.pipeliet.bolts.dto.grafana;

public class AnnotationRequest {
    private Range range;
    private RangeRaw rangeRaw;
    private Annotation annotation;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public RangeRaw getRangeRaw() {
        return rangeRaw;
    }

    public void setRangeRaw(RangeRaw rangeRaw) {
        this.rangeRaw = rangeRaw;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public String toString() {
        return "AnnotationRequest{" +
                "range=" + range +
                ", rangeRaw=" + rangeRaw +
                ", annotation=" + annotation +
                '}';
    }
}
