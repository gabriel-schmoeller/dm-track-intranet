package ponto.intranet.handlers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ponto.intranet.service.domain.DailyReport;
import ponto.intranet.service.domain.MonthReport;
import ponto.intranet.persistence.domain.DailyTimeReport;
import ponto.intranet.persistence.domain.TimeByDay;
import ponto.intranet.persistence.domain.TimeTracking;

/**
 * @author gabriel.schmoeller
 */
@Component
public class DailyReportFactory {

    private final TimeHandler timeHandler;

    @Autowired
    public DailyReportFactory(TimeHandler timeHandler) {
        this.timeHandler = timeHandler;
    }

    public List<DailyTimeReport> create(List<TimeTracking> timesTracking, List<TimeByDay> timeByDays) {
        List<DailyTimeReport> dailyTimeReports = new ArrayList<>();

        for (TimeTracking timeTracking : timesTracking) {
            Duration timeTrack = timeHandler.calculateTotalTrack(timeTracking.getTracks());
            Duration timeJustify = timeHandler.calculateTotalJustify(timeTracking.getJustified());
            Duration timeOfDay = timeTrack.plus(timeJustify);
            Duration balance;

            if (timeTracking.isFreeDay()) {
                balance = timeOfDay;
            } else {
                balance = timeOfDay.minus(getTimeByDay(timeTracking.getDate(), timeByDays));
            }

            dailyTimeReports.add(new DailyTimeReport(timeTracking, timeTrack, timeJustify, timeOfDay, balance));
        }

        return dailyTimeReports;
    }

    public List<MonthReport> toMonthsReport(Collection<DailyTimeReport> dailyTimeReports) {
        List<MonthReport> monthReports = new ArrayList<>();

        Map<Integer, List<DailyTimeReport>> reportMap = new HashMap<>();

        for (DailyTimeReport dailyTimeReport : dailyTimeReports) {
            int month = dailyTimeReport.getDate().getMonthValue();
            if (dailyTimeReport.getDate().getDayOfMonth() > 15) {
                month = dailyTimeReport.getDate().plusMonths(1).getMonthValue();
            }

            reportMap.putIfAbsent(month, new ArrayList<>());
            reportMap.get(month).add(dailyTimeReport);
        }

        for (Map.Entry<Integer, List<DailyTimeReport>> reportEntry : reportMap.entrySet()) {
            int months = reportEntry.getKey();
            String balance =  timeHandler.formatDuration(timeHandler.calculateBalance(reportEntry.getValue()));
            List<DailyReport> dailyReports = toViewBean(reportEntry.getValue());

            monthReports.add(new MonthReport(months, balance, dailyReports));
        }

        return monthReports.stream().sorted((o1, o2) -> o1.compareTo(o2) * -1).collect(Collectors.toList());
    }

    public List<DailyReport> toViewBean(Collection<DailyTimeReport> dailyReports) {
        return dailyReports.stream().map(this::toViewBean).sorted().collect(Collectors.toList());
    }

    public DailyReport toViewBean(DailyTimeReport dailyReport) {
        ZonedDateTime date = dailyReport.getDate().atStartOfDay(ZoneId.systemDefault());
        List<LocalTime> timeTracks = dailyReport.getTimeTracking().getTracks();
        String timeByTrack = timeHandler.formatDuration(dailyReport.getTimeByTracking());
        String justified = timeHandler.formatDuration(dailyReport.getTimeJustify());
        String balance = timeHandler.formatDuration(dailyReport.getBalance());
        boolean freeDay = dailyReport.getTimeTracking().isFreeDay();
        boolean incomplete = dailyReport.getTimeTracking().isIncomplete();
        String note = dailyReport.getTimeTracking().getNote();

        return new DailyReport(date, timeTracks, timeByTrack, justified, balance, note, freeDay, incomplete);
    }

    private Duration getTimeByDay(LocalDate date, List<TimeByDay> timeByDays) {
        for (TimeByDay timeByDay : timeByDays) {
            if (!(date.isBefore(timeByDay.getFtDate()) || date.isAfter(timeByDay.getLtDate()))) {
                return timeByDay.getTimeByDay();
            }
        }

        return TimeByDay.DEFAULT_DURATION;
    }
}
