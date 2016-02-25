package ponto.intranet.service.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author gabriel.schmoeller
 */
public class TodayReport {

    private List<LocalTime> timeTracks;
    private LocalTime estimatedEnd;
    private ZonedDateTime balanceCalcFtDate;
    private ZonedDateTime balanceCalcLtDate;
    private String balanceCalcResult;

    public TodayReport() {
    }

    public TodayReport(List<LocalTime> timeTracks, LocalTime estimatedEnd, LocalDate balanceCalcFtDate,
            LocalDate balanceCalcLtDate, String balanceCalcResult) {
        this.timeTracks = timeTracks;
        this.estimatedEnd = estimatedEnd;
        this.balanceCalcFtDate = balanceCalcFtDate.atStartOfDay(ZoneId.systemDefault());
        this.balanceCalcLtDate = balanceCalcLtDate.atStartOfDay(ZoneId.systemDefault());
        this.balanceCalcResult = balanceCalcResult;
    }

    public List<LocalTime> getTimeTracks() {
        return timeTracks;
    }

    public LocalTime getEstimatedEnd() {
        return estimatedEnd;
    }

    public ZonedDateTime getBalanceCalcFtDate() {
        return balanceCalcFtDate;
    }

    public ZonedDateTime getBalanceCalcLtDate() {
        return balanceCalcLtDate;
    }

    public String getBalanceCalcResult() {
        return balanceCalcResult;
    }
}
