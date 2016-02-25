package ponto.intranet.service.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import ponto.intranet.persistence.domain.DailyTimeReport;
import ponto.intranet.persistence.domain.TimeByDay;

/**
 * @author gabriel.schmoeller
 */
public class TimeReport {

    private List<DailyTimeReport> dailyTimeReports;
    private Duration initialTime;
    private LocalDate dayAtInitialTime;
    private List<TimeByDay> timeByDays;
    private Duration totalBalance;

    public TimeReport(List<DailyTimeReport> dailyTimeReports, Duration initialTime, LocalDate dayAtInitialTime,
            List<TimeByDay> timeByDays, Duration totalBalance) {
        this.dailyTimeReports = dailyTimeReports;
        this.initialTime = initialTime;
        this.dayAtInitialTime = dayAtInitialTime;
        this.timeByDays = timeByDays;
        this.totalBalance = totalBalance;
    }

    public List<DailyTimeReport> getDailyTimeReports() {
        return dailyTimeReports;
    }

    public Duration getInitialTime() {
        return initialTime;
    }

    public LocalDate getDayAtInitialTime() {
        return dayAtInitialTime;
    }

    public List<TimeByDay> getTimeByDays() {
        return timeByDays;
    }

    public Duration getTotalBalance() {
        return totalBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeReport)) {
            return false;
        }
        TimeReport that = (TimeReport) o;
        return Objects.equals(dailyTimeReports, that.dailyTimeReports)
                && Objects.equals(initialTime, that.initialTime)
                && Objects.equals(dayAtInitialTime, that.dayAtInitialTime)
                && Objects.equals(timeByDays, that.timeByDays)
                && Objects.equals(totalBalance, that.totalBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dailyTimeReports, initialTime, dayAtInitialTime, timeByDays, totalBalance);
    }

    @Override
    public String toString() {
        return "TimeReport{" +
                "timeTrackings=" + dailyTimeReports +
                ", initialTime=" + initialTime +
                ", initialDay=" + dayAtInitialTime +
                ", trackByDay=" + timeByDays +
                ", totalTime=" + totalBalance +
                '}';
    }
}
