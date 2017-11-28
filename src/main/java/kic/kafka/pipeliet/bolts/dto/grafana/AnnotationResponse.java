package kic.kafka.pipeliet.bolts.dto.grafana;

public class AnnotationResponse {
    private long time;
    private String title;
    private Annotation annotation;

    public AnnotationResponse() {
    }

    public AnnotationResponse(long time, String title, Annotation annotation) {
        this.time = time;
        this.title = title;
        this.annotation = annotation;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public String toString() {
        return "AnnotationResponse{" +
                "time=" + time +
                ", title='" + title + '\'' +
                ", annotation=" + annotation +
                '}';
    }
}
