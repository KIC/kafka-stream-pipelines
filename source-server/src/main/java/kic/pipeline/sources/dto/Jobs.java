package kic.pipeline.sources.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Jobs {
    public final List<Job> jobs;

    @JsonCreator
    public Jobs(@JsonProperty("jobs") List<Job> jobs) {
        this.jobs = Collections.unmodifiableList(jobs);
    }

    public static class Job {
        public final String id;
        public final Schedule schedule;
        public final String topic;
        public final String dir;
        public final String[] env;
        public final List<String> command;
        public final Map<String, String> defaultArgumentValues;
        public final List<String> keyExtractor;
        public final List<String> valueExtractor;
        public final String encoding;

        @JsonCreator
        public Job(@JsonProperty("id") String id,
                   @JsonProperty("schedule") Schedule schedule,
                   @JsonProperty("topic") String topic,
                   @JsonProperty("dir") String dir,
                   @JsonProperty("env") String[] env,
                   @JsonProperty("command") List<String> command,
                   @JsonProperty("defaultArgumentValues") Map<String, String> defaultArgumentValues,
                   @JsonProperty("keyExtractor") List<String> keyExtractor,
                   @JsonProperty("valueExtractor") List<String> valueExtractor,
                   @JsonProperty("encoding") String encoding
        ) {
            this.id = id;
            this.schedule = schedule;
            this.topic = topic;
            this.dir = dir;
            this.env = env;
            this.command = Collections.unmodifiableList(command);
            this.defaultArgumentValues = defaultArgumentValues;
            this.keyExtractor = Collections.unmodifiableList(keyExtractor);
            this.valueExtractor = Collections.unmodifiableList(valueExtractor);
            this.encoding = encoding;
        }
    }

    public static class Schedule {
        public final String minutes;
        public final String hours;
        public final String days;
        public final String month;
        public final String daysOfWeek;

        @JsonCreator
        public Schedule(@JsonProperty("minutes") String minutes,
                        @JsonProperty("hours") String hours,
                        @JsonProperty("days") String days,
                        @JsonProperty("month") String month,
                        @JsonProperty("daysOfWeek") String daysOfWeek
        ) {
            this.minutes = minutes != null ? minutes : "*";
            this.hours = hours != null ? hours : "*";
            this.days = days != null ? days : "*";
            this.month = month != null ? month : "*";
            this.daysOfWeek = daysOfWeek != null ? daysOfWeek : "*";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Schedule schedule = (Schedule) o;
            return Objects.equals(minutes, schedule.minutes) &&
                    Objects.equals(hours, schedule.hours) &&
                    Objects.equals(days, schedule.days) &&
                    Objects.equals(month, schedule.month) &&
                    Objects.equals(daysOfWeek, schedule.daysOfWeek);
        }

        @Override
        public int hashCode() {

            return Objects.hash(minutes, hours, days, month, daysOfWeek);
        }

        @Override
        public String toString() {
            return minutes + " " +
                   hours + " " +
                   days + " " +
                   month + " " +
                   daysOfWeek;
        }
    }
}
