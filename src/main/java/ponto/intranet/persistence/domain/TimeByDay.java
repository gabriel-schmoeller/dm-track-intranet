package ponto.intranet.persistence.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

/**
 * @author gabriel.schmoeller
 */
public class TimeByDay {

    public static Duration DEFAULT_DURATION = Duration.ofHours(8).plusMinutes(30);

    private LocalDate ftDate;
    private LocalDate ltDate;
    private Duration timeByDay;

    public TimeByDay() {
    }

    public TimeByDay(LocalDate ftDate, LocalDate ltDate, Duration timeByDay) {
        this.ftDate = ftDate;
        this.ltDate = ltDate;
        this.timeByDay = timeByDay;
    }

    public LocalDate getFtDate() {
        return ftDate;
    }

    public LocalDate getLtDate() {
        return ltDate;
    }

    public Duration getTimeByDay() {
        return timeByDay;
    }

    public boolean isBetween(LocalDate date) {
        return !(ftDate.isAfter(date) || ltDate.isBefore(date));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeByDay)) {
            return false;
        }
        TimeByDay timeByDay1 = (TimeByDay) o;
        return Objects.equals(ftDate, timeByDay1.ftDate)
                && Objects.equals(ltDate, timeByDay1.ltDate)
                && Objects.equals(timeByDay, timeByDay1.timeByDay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ftDate, ltDate, timeByDay);
    }

    @Override
    public String toString() {
        return "TimeByDay{" +
                "ftDate=" + ftDate +
                ", ltDate=" + ltDate +
                ", timeByDay=" + timeByDay +
                '}';
    }
}
