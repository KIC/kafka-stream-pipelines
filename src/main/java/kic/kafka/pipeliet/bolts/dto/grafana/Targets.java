package kic.kafka.pipeliet.bolts.dto.grafana;

public class Targets {
    private String target;
    private String refId;
    private String type;

    public void setTarget(String target) {
         this.target = target;
     }

    public String getTarget() {
         return target;
     }

    public void setRefId(String refId) {
         this.refId = refId;
     }

    public String getRefId() {
         return refId;
     }

    public void setType(String type) {
         this.type = type;
     }

    public String getType() {
         return type;
    }

    @Override
    public String toString() {
        return "Targets{" +
                "target='" + target + '\'' +
                ", refId='" + refId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}