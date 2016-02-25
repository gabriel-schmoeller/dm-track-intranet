package ponto.intranet.persistence.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * @author gabriel.schmoeller
 */
public class TimeTracking implements Comparable<TimeTracking> {

    private LocalDate date;
    private List<LocalTime> tracks;
    private List<LocalTime> justified;
    private boolean freeDay;
    private boolean incomplete;
    private String note;

    public TimeTracking() {
    }

    public TimeTracking(LocalDate date, List<LocalTime> tracks, List<LocalTime> justified, boolean freeDay, boolean incomplete) {
        this(date, tracks, justified, freeDay, incomplete, "");
    }

    public TimeTracking(LocalDate date, List<LocalTime> tracks, List<LocalTime> justified, boolean freeDay, boolean incomplete,
            String note) {
        this.date = date;
        this.tracks = tracks;
        this.justified = justified;
        this.freeDay = freeDay;
        this.incomplete = incomplete;
        this.note = note;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<LocalTime> getTracks() {
        return tracks;
    }

    public List<LocalTime> getJustified() {
        return justified;
    }

    public boolean isFreeDay() {
        return freeDay;
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public void setIncomplete(boolean incomplete) {
        this.incomplete = incomplete;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean hasNoChanges(TimeTracking tt) {
        return Objects.equals(date, tt.date)
                && Objects.equals(tracks, tt.tracks)
                && Objects.equals(justified, tt.justified)
                && Objects.equals(freeDay, tt.freeDay);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeTracking)) {
            return false;
        }
        TimeTracking ponto = (TimeTracking) o;
        return Objects.equals(date, ponto.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    @Override
    public String toString() {
        return "TimeTracking{" +
                "date=" + date +
                ", tracks=" + tracks +
                ", justified=" + justified +
                ", freeDay=" + freeDay +
                ", incomplete=" + incomplete +
                ", note=" + note +
                '}';
    }

    @Override
    public int compareTo(TimeTracking o) {
        return this.getDate().compareTo(o.getDate());
    }
}
