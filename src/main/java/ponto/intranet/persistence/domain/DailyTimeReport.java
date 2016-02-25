package ponto.intranet.persistence.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author gabriel.schmoeller
 */
public class DailyTimeReport implements Comparable<DailyTimeReport> {

    private TimeTracking timeTracking;
    private Duration timeByTracking;
    private Duration timeJustify;
    private Duration timeOfDay;
    private Duration balance;

    public DailyTimeReport() {
    }

    public DailyTimeReport(TimeTracking timeTracking, Duration timeByTracking, Duration timeJustify, Duration timeOfDay,
            Duration balance) {
        this.timeTracking = timeTracking;
        this.timeByTracking = timeByTracking;
        this.timeJustify = timeJustify;
        this.timeOfDay = timeOfDay;
        this.balance = balance;
    }

    @JsonIgnore
    public LocalDate getDate() {
        return timeTracking.getDate();
    }

    public TimeTracking getTimeTracking() {
        return timeTracking;
    }

    public Duration getTimeByTracking() {
        return timeByTracking;
    }

    public Duration getTimeJustify() {
        return timeJustify;
    }

    public Duration getTimeOfDay() {
        return timeOfDay;
    }

    public Duration getBalance() {
        return balance;
    }

    public boolean hasNoChanges(DailyTimeReport report) {
        return getTimeTracking().hasNoChanges(report.getTimeTracking());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DailyTimeReport)) {
            return false;
        }
        DailyTimeReport that = (DailyTimeReport) o;
        return Objects.equals(timeTracking, that.timeTracking);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeTracking);
    }

    @Override
    public String toString() {
        return "DailyTimeReport{" +
                "timeTracking=" + timeTracking +
                ", timeByTracking=" + timeByTracking +
                ", timeJustify=" + timeJustify +
                ", timeOfDay=" + timeOfDay +
                ", balance=" + balance +
                '}';
    }

    @Override
    public int compareTo(DailyTimeReport o) {
        return getDate().compareTo(o.getDate());
    }
}
