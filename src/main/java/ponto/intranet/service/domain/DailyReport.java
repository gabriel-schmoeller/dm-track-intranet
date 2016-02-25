package ponto.intranet.service.domain;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author gabriel.schmoeller
 */
public class DailyReport implements Comparable<DailyReport> {

    private ZonedDateTime date;
    private List<LocalTime> timeTracks;
    private String timeByTracks;
    private String justified;
    private String balance;
    private String note;
    private boolean freeDay;
    private boolean incomplete;

    public DailyReport() {
    }

    public DailyReport(ZonedDateTime date, List<LocalTime> timeTracks, String timeByTracks, String justified, String balance,
            String note, boolean freeDay, boolean incomplete) {
        this.date = date;
        this.timeTracks = timeTracks;
        this.timeByTracks = timeByTracks;
        this.justified = justified;
        this.balance = balance;
        this.note = note;
        this.freeDay = freeDay;
        this.incomplete = incomplete;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public List<LocalTime> getTimeTracks() {
        return timeTracks;
    }

    public String getTimeByTracks() {
        return timeByTracks;
    }

    public String getJustified() {
        return justified;
    }

    public String getBalance() {
        return balance;
    }

    public boolean isFreeDay() {
        return freeDay;
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public String getNote() {
        return note;
    }

    @Override
    public int compareTo(DailyReport o) {
        return date.compareTo(o.getDate());
    }
}
