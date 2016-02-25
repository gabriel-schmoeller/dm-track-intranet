package ponto.intranet.persistence;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ponto.intranet.persistence.domain.DailyTimeReport;
import ponto.intranet.persistence.domain.TimeByDay;
import ponto.intranet.persistence.domain.IntranetEntity;
import ponto.intranet.persistence.domain.TimeTracking;
import ponto.intranet.handlers.DailyReportFactory;

/**
 * @author gabriel.schmoeller
 */
@Component
public class IntranetDao {

    private final IntranetEntityManager entityManager;
    private final DailyReportFactory dailyReportFactory;

    @Autowired
    public IntranetDao(IntranetEntityManager entityManager, DailyReportFactory dailyReportFactory) {
        this.entityManager = entityManager;
        this.dailyReportFactory = dailyReportFactory;
    }

    public IntranetEntity load(String username) {
        return entityManager.getEntity(username);
    }

    public void persist(IntranetEntity entity) {
        entityManager.persist(entity);
    }

    public DailyTimeReport replaceDate(String username, TimeTracking timeTracking) {
        List<TimeTracking> timeTrackings = new ArrayList<>();
        timeTrackings.add(timeTracking);

        return replaceDates(username, timeTrackings).get(0);
    }

    public List<DailyTimeReport> replaceDates(String username, List<TimeTracking> timeTrackings) {
        IntranetEntity entity = load(username);
        Set<DailyTimeReport> dailyTimeReports = entity.getDailyTimeReports();
        List<DailyTimeReport> requestedReports = dailyReportFactory.create(timeTrackings, entity.getTimeByDays());

        dailyTimeReports.removeAll(requestedReports);
        dailyTimeReports.addAll(requestedReports);
        persist(entity);

        return requestedReports;
    }

    public Optional<DailyTimeReport> addDate(String username, TimeTracking timeTracking) {
        List<TimeTracking> timeTrackings = new ArrayList<>();
        timeTrackings.add(timeTracking);

        return addDates(username, timeTrackings).stream().findFirst();
    }

    public List<DailyTimeReport> addDates(String username, List<TimeTracking> timeTrackings) {
        IntranetEntity entity = load(username);
        Set<DailyTimeReport> dailyTimeReports = entity.getDailyTimeReports();
        List<DailyTimeReport> requestedReports = dailyReportFactory.create(timeTrackings, entity.getTimeByDays());

        List<DailyTimeReport> changedReports = requestedReports.stream()
                .filter(report -> !dailyTimeReports.stream().anyMatch(report::hasNoChanges))
                .collect(Collectors.toList());

        if (!changedReports.isEmpty()) {
            dailyTimeReports.removeAll(changedReports);
            dailyTimeReports.addAll(changedReports);
            persist(entity);
        }

        return changedReports;
    }

    public void setInitialTime(String username, Duration initialTime, LocalDate dayAtInitialTime) {
        IntranetEntity entity = load(username);

        entity.setInitialTime(initialTime);
        entity.setDayAtInitialTime(dayAtInitialTime);

        persist(entity);
    }

    public Optional<DailyTimeReport> getDate(String username, LocalDate date) {
        IntranetEntity entity = load(username);

        return entity.getDailyTimeReports().stream().filter(report -> report.getDate().equals(date)).findFirst();
    }

    public LocalDate getFirstDay(String username) {
        IntranetEntity entity = load(username);
        return extractFirstDay(entity);
    }

    public Set<DailyTimeReport> getDailyReportsByYear(String username, int year) {
        IntranetEntity entity = load(username);

        return entity.getDailyTimeReports().stream()
                .filter(report -> report.getDate().isAfter(LocalDate.of(year - 1, 12, 15))
                        && report.getDate().isBefore(LocalDate.of(year, 12, 16)))
                .collect(Collectors.toSet());
    }

    public List<Integer> getYears(String username) {
        IntranetEntity entity = load(username);

        return entity.getDailyTimeReports().stream()
                .map(dailyTimeReport -> dailyTimeReport.getDate().getYear())
                .collect(Collectors.toSet()).stream()
                .sorted((o1, o2) -> o1.compareTo(o2) * -1).collect(Collectors.toList());
    }

    public void setIncomplete(String username, LocalDate date, boolean incomplete) {
        IntranetEntity entity = load(username);

        Optional<DailyTimeReport> dailyTimeReport =
                entity.getDailyTimeReports().stream().filter(report -> report.getDate().equals(date)).findFirst();

        if (dailyTimeReport.isPresent()) {
            TimeTracking timeTracking = dailyTimeReport.get().getTimeTracking();
            timeTracking.setIncomplete(incomplete);

            replaceDate(username, timeTracking);
        }
    }

    public void setNote(String username, LocalDate date, String note) {
        IntranetEntity entity = load(username);

        Optional<DailyTimeReport> dailyTimeReport =
                entity.getDailyTimeReports().stream().filter(report -> report.getDate().equals(date)).findFirst();

        if (dailyTimeReport.isPresent()) {
            TimeTracking timeTracking = dailyTimeReport.get().getTimeTracking();
            timeTracking.setNote(note);

            replaceDate(username, timeTracking);
        }
    }

    private LocalDate extractFirstDay(IntranetEntity entity) {
        return entity.getDailyTimeReports().stream().map(DailyTimeReport::getDate).sorted().findFirst()
                .orElse(LocalDate.now().plusDays(1));
    }

    public LocalDate getLastDate(String username) {
        IntranetEntity entity = load(username);
        return entity.getDailyTimeReports().stream().map(DailyTimeReport::getDate)
                .sorted((o1, o2) -> o1.compareTo(o2) * -1).findFirst().orElse(LocalDate.now());
    }

    public List<TimeByDay> getTimeByDays(String username) {
        IntranetEntity entity = load(username);
        return entity.getTimeByDays();
    }

    public Set<DailyTimeReport> getDailyReports(String username) {
        IntranetEntity entity = load(username);
        return entity.getDailyTimeReports();
    }

    public LocalDate getDayAtInitialTime(String username) {
        IntranetEntity entity = load(username);
        return entity.getDayAtInitialTime();
    }

    public Duration getInitialTime(String username) {
        IntranetEntity entity = load(username);
        return entity.getInitialTime();
    }

    public List<LocalDate> getDatesToUpdate(String username) {
        IntranetEntity entity = load(username);
        List<LocalDate> datesToUpdate = filterIncompletes(entity);
        datesToUpdate.addAll(getAbsents(entity));

        return datesToUpdate;
    }

    private List<LocalDate> getAbsents(IntranetEntity entity) {
        LocalDate firstDay = extractFirstDay(entity);
        LocalDate aux = LocalDate.now();
        Set<LocalDate> absentDates = new HashSet<>();

        while (aux.isAfter(firstDay)) {
            absentDates.add(aux);
            aux = aux.minusDays(1);
        }

        absentDates.removeAll(entity.getDailyTimeReports().stream().map(DailyTimeReport::getDate).collect(Collectors.toSet()));

        return absentDates.stream().collect(Collectors.toList());
    }

    private List<LocalDate> filterIncompletes(IntranetEntity entity) {
        return entity.getDailyTimeReports().stream()
                .filter(dailyTimeReport -> dailyTimeReport.getTimeTracking().isIncomplete())
                .map(DailyTimeReport::getDate).collect(Collectors.toList());
    }
}
