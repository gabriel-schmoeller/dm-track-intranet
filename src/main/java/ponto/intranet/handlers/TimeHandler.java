package ponto.intranet.handlers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import ponto.intranet.persistence.domain.DailyTimeReport;
import ponto.intranet.persistence.domain.TimeByDay;

/**
 * @author gabriel.schmoeller
 */
@Component
public class TimeHandler {

    public Duration calculateBalance(LocalDate ftDate, LocalDate ltDate, Collection<DailyTimeReport> dailyTimeReports) {
        Duration actual = Duration.ZERO;

        for (DailyTimeReport dailyTimeReport : dailyTimeReports) {
            if (!dailyTimeReport.getTimeTracking().isIncomplete()
                    && ftDate.minusDays(1).isBefore(dailyTimeReport.getDate())
                    && ltDate.plusDays(1).isAfter(dailyTimeReport.getDate())) {
                actual = actual.plus(dailyTimeReport.getBalance());
            }
        }

        return actual;
    }

    public Duration calculateBalance(Collection<DailyTimeReport> dailyTimeReports) {
        Duration actual = Duration.ZERO;

        for (DailyTimeReport dailyTimeReport : dailyTimeReports) {
            if (!dailyTimeReport.getTimeTracking().isIncomplete()) {
                actual = actual.plus(dailyTimeReport.getBalance());
            }
        }

        return actual;
    }

    public Duration calculateTotalTrack(List<LocalTime> tracks) {
        Duration totalTrack = Duration.ZERO;

        for (int i = 0; i < tracks.size(); i++) {
            if (((i+1) % 2) == 0) {
                totalTrack = totalTrack.plus(Duration.between(tracks.get(i-1), tracks.get(i)));
            }
        }

        return totalTrack;
    }

    public Duration calculateTotalJustify(List<LocalTime> justified) {
        Duration totalJustify = Duration.ofDays(0);

        for (LocalTime time : justified) {
            totalJustify = totalJustify.plusHours(time.getHour());
            totalJustify = totalJustify.plusMinutes(time.getMinute());
        }

        return totalJustify;
    }

    public LocalTime calculateLastTrack(List<TimeByDay> timeByDays, DailyTimeReport dailyTimeReport) {
        List<LocalTime> tracks = dailyTimeReport.getTimeTracking().getTracks();
        Duration durationOfDay = timeByDays.stream().filter(timeByDay -> timeByDay.isBetween(dailyTimeReport.getDate())).map(
                TimeByDay::getTimeByDay).findFirst().orElse(TimeByDay.DEFAULT_DURATION);

        if (tracks.isEmpty()) {
            return LocalTime.of(8, 0).plus(durationOfDay);
        } else if (tracks.size() <= 2) {
            return tracks.get(0).plus(durationOfDay).plusHours(1);
        } else {
            Duration duration = dailyTimeReport.getTimeByTracking();

            return tracks.get(tracks.size()-1).plus(durationOfDay.minus(duration));
        }
    }

    public LocalDate timesheetBeginning() {
        LocalDate today = LocalDate.now();

        if (today.getDayOfMonth() > 15) {
            return today.withDayOfMonth(16);
        } else {
            return today.minusMonths(1).withDayOfMonth(16);
        }
    }

    public String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = Math.abs(duration.minusHours(hours).toMinutes());
        hours = Math.abs(hours);

        if (duration.isNegative()) {
            return String.format("-%02d:%02d", hours, minutes);
        }
        return String.format("%02d:%02d", hours, minutes);
    }

    public Duration parseDuration(String durationString) {
        durationString = durationString.trim();

        if(durationString.matches("-?\\d+:\\d+")) {
            String[] time = durationString.split(":");

            if (durationString.charAt(0) == '-') {
                return Duration.ofHours(Long.parseLong(time[0])).minusMinutes(Long.parseLong(time[1]));
            } else {
                return Duration.ofHours(Long.parseLong(time[0])).plusMinutes(Long.parseLong(time[1]));
            }
        }

        throw new IllegalArgumentException(durationString + " is a invalid duration format");
    }
}
