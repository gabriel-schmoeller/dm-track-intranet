package ponto.intranet.persistence.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author gabriel.schmoeller
 */
public class IntranetEntity {

    private String username;
    private Set<DailyTimeReport> dailyTimeReports;
    private List<TimeByDay> timeByDays;
    private Duration initialTime;
    private LocalDate dayAtInitialTime;

    public IntranetEntity() {
    }

    public IntranetEntity(String username, Set<DailyTimeReport> dailyTimeReports, List<TimeByDay> timeByDays, Duration initialTime,
            LocalDate dayAtInitialTime) {
        this.username = username;
        this.dailyTimeReports = dailyTimeReports;
        this.timeByDays = timeByDays;
        this.initialTime = initialTime;
        this.dayAtInitialTime = dayAtInitialTime;
    }

    public String getUsername() {
        return username;
    }

    public Set<DailyTimeReport> getDailyTimeReports() {
        return dailyTimeReports;
    }

    public List<TimeByDay> getTimeByDays() {
        return timeByDays;
    }

    public Duration getInitialTime() {
        return initialTime;
    }

    public LocalDate getDayAtInitialTime() {
        return dayAtInitialTime;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDailyTimeReports(Set<DailyTimeReport> dailyTimeReports) {
        this.dailyTimeReports = dailyTimeReports;
    }

    public void setTimeByDays(List<TimeByDay> timeByDays) {
        this.timeByDays = timeByDays;
    }

    public void setInitialTime(Duration initialTime) {
        this.initialTime = initialTime;
    }

    public void setDayAtInitialTime(LocalDate dayAtInitialTime) {
        this.dayAtInitialTime = dayAtInitialTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntranetEntity)) {
            return false;
        }
        IntranetEntity that = (IntranetEntity) o;
        return Objects.equals(username, that.username)
                && Objects.equals(dailyTimeReports, that.dailyTimeReports)
                && Objects.equals(timeByDays, that.timeByDays)
                && Objects.equals(initialTime, that.initialTime)
                && Objects.equals(dayAtInitialTime, that.dayAtInitialTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, dailyTimeReports, timeByDays, initialTime, dayAtInitialTime);
    }

    @Override
    public String toString() {
        return "IntranetEntity{" +
                "username=" + username +
                ", timesTracking=" + dailyTimeReports +
                ", timeByDays=" + timeByDays +
                ", initialTime=" + initialTime +
                ", dayAtInitialTime=" + dayAtInitialTime +
                '}';
    }
}
