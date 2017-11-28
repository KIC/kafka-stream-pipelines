package kic.kafka.pipeliet.bolts.dto.grafana;

public class Annotation {
    private Range range;
    private RangeRaw rangeRaw;
    private String name;
    private String datasource;
    private String iconColor;
    private boolean enable;
    private String query;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "range=" + range +
                ", rangeRaw=" + rangeRaw +
                ", name='" + name + '\'' +
                ", datasource='" + datasource + '\'' +
                ", iconColor='" + iconColor + '\'' +
                ", enable=" + enable +
                ", query='" + query + '\'' +
                '}';
    }
}
