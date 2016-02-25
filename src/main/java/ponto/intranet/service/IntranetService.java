package ponto.intranet.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ponto.intranet.service.domain.BalanceReport;
import ponto.intranet.service.domain.DailyReport;
import ponto.intranet.service.domain.Date;
import ponto.intranet.service.domain.DatePeriod;
import ponto.intranet.service.domain.InitialBalance;
import ponto.intranet.service.domain.LocalData;
import ponto.intranet.service.domain.MonthReport;
import ponto.intranet.service.domain.TicketTemplate;
import ponto.intranet.service.domain.TicketsAndLinks;
import ponto.intranet.service.domain.TimeContract;
import ponto.intranet.service.domain.TodayReport;
import ponto.intranet.service.domain.UserCredentials;
import ponto.intranet.persistence.domain.DailyTimeReport;
import ponto.intranet.handlers.domain.SessionContext;
import ponto.intranet.service.domain.Ticket;
import ponto.intranet.persistence.domain.TimeByDay;
import ponto.intranet.persistence.domain.TimeTracking;
import ponto.intranet.exception.LoginCredentialsException;
import ponto.intranet.handlers.DailyReportFactory;
import ponto.intranet.handlers.IntranetClient;
import ponto.intranet.handlers.TimeHandler;
import ponto.intranet.persistence.IntranetDao;

/**
 * @author gabriel.schmoeller
 */
@Component
@Scope(proxyMode= ScopedProxyMode.TARGET_CLASS, value="session")
public class IntranetService {

    private final IntranetServiceValidator validator;
    private final IntranetClient intranetClient;
    private final TimeHandler timeHandler;
    private final SessionContext context;
    private final IntranetDao intranetDao;
    private final DailyReportFactory dailyReportFactory;
    private final TicketTypeTemplateFactory ticketTemplateFactory;

    @Autowired
    public IntranetService(IntranetServiceValidator validator, IntranetClient intranetClient, TimeHandler timeHandler,
            SessionContext context, IntranetDao intranetDao, DailyReportFactory dailyReportFactory,
            TicketTypeTemplateFactory ticketTemplateFactory) {
        this.validator = validator;
        this.intranetClient = intranetClient;
        this.timeHandler = timeHandler;
        this.context = context;
        this.intranetDao = intranetDao;
        this.dailyReportFactory = dailyReportFactory;
        this.ticketTemplateFactory = ticketTemplateFactory;
    }

    public void login(UserCredentials user) throws LoginCredentialsException {
        validator.validateLogin(user);
        intranetClient.login(user.getUser(), user.getPass(), context);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUser(), user.getUser(),
                        AuthorityUtils.createAuthorityList("user")));

        updateLocalData();
    }

    public void logout() {
        SecurityContextHolder.clearContext();
        intranetClient.logout(context);
    }

    public void setInitialBalance(InitialBalance initialBalance) {
        validator.validateInitialBalance(initialBalance);
        String username = context.getUsername();
        LocalDate dayAtInitialTime = initialBalance.getInitialDate().toLocalDate();
        Duration initialTime = timeHandler.parseDuration(initialBalance.getInitialBalance());

        intranetDao.setInitialTime(username, initialTime, dayAtInitialTime);
    }

    public String login() {
        updateLocalData();
        return context.getUsername();
    }

    public List<DailyReport> getDailyReports() {
        Set<DailyTimeReport> dailyReports = intranetDao.getDailyReports(context.getUsername());

        return dailyReportFactory.toViewBean(dailyReports);
    }

    public List<MonthReport> getMonthReport(int year) {
        Set<DailyTimeReport> dailyReports = intranetDao.getDailyReportsByYear(context.getUsername(), year);

        return dailyReportFactory.toMonthsReport(dailyReports);
    }

    public List<TimeContract> getTimeContract() {
        List<TimeContract> timeContracts = intranetDao.getTimeByDays(context.getUsername()).stream()
                .map(timeByDay -> new TimeContract(timeByDay.getFtDate().atStartOfDay(ZoneId.systemDefault()),
                        timeByDay.getLtDate().atStartOfDay(ZoneId.systemDefault()),
                        timeHandler.formatDuration(timeByDay.getTimeByDay()))).collect(Collectors.toList());
        timeContracts.add(new TimeContract(null, null, timeHandler.formatDuration(TimeByDay.DEFAULT_DURATION)));

        return timeContracts;
    }

    public boolean updateDay(Date date) {
        TimeTracking day = intranetClient.getDay(date.getDate().toLocalDate(), context);

        return intranetDao.addDate(context.getUsername(), day).isPresent();
    }

    public void markAsIncomplete(Date date) {
        intranetDao.setIncomplete(context.getUsername(), date.getDate().toLocalDate(), true);
    }

    public void marksAsComplete(Date date) {
        intranetDao.setIncomplete(context.getUsername(), date.getDate().toLocalDate(), false);
    }

    public void setNote(DailyReport report) {
        intranetDao.setNote(context.getUsername(), report.getDate().toLocalDate(), report.getNote());
    }

    public List<TicketTemplate> getTicketTypes() {
        return ticketTemplateFactory.create();
    }

    public TicketsAndLinks openTicket(Ticket ticket) {
        Integer ticketNo = intranetClient.openTicket(ticket, context);
        List<String> linksToTicket = intranetClient.linksToTicket(ticketNo, context);

        return new TicketsAndLinks(ticketNo, linksToTicket);
    }

    public boolean linkAtTicket(TicketsAndLinks ticket) {
        return  intranetClient.linkAtTicket(ticket, context);
    }

    private void updateLocalData() {
        if (intranetDao.getDailyReports(context.getUsername()).isEmpty()) {
            downloadMonths(1);
        } else {
            List<LocalDate> datesToUpdate = intranetDao.getDatesToUpdate(context.getUsername());
            List<TimeTracking> timeTrackings = intranetClient.getDates(context, datesToUpdate);

            intranetDao.addDates(context.getUsername(), timeTrackings);
        }
    }

    public LocalData downloadMonths(int months) {
        validator.validateDownloadMonths(months);
        LocalDate ltDate = intranetDao.getFirstDay(context.getUsername());
        LocalDate stDate = ltDate.minusMonths(months).withDayOfMonth(16);

        List<TimeTracking> timeTrackings = intranetClient.getPeriod(stDate, ltDate, context);
        intranetDao.addDates(context.getUsername(), timeTrackings);

        return getLocalData();
    }

    public LocalData getLocalData() {
        ZonedDateTime ftLocalDate = intranetDao.getFirstDay(context.getUsername()).atStartOfDay(ZoneId.systemDefault());
        List<Integer> localYears = intranetDao.getYears(context.getUsername());

        return new LocalData(ftLocalDate, localYears);
    }

    public TodayReport getTodayReport() {
        LocalDate today = LocalDate.now();
        TimeTracking timeTracking = intranetClient.getDay(today, context);
        DailyTimeReport todayDailyTimeReport = intranetDao.addDate(context.getUsername(), timeTracking)
                .orElse(intranetDao.getDate(context.getUsername(), today).get());
        List<LocalTime> todayTracks = todayDailyTimeReport.getTimeTracking().getTracks();

        List<TimeByDay> timeByDays = intranetDao.getTimeByDays(context.getUsername());
        LocalTime estimatedEnd = timeHandler.calculateLastTrack(timeByDays, todayDailyTimeReport);

        LocalDate balanceCalcFtDate = timeHandler.timesheetBeginning();
        Duration balanceCalcResultDuration = calculateBalance(balanceCalcFtDate, today);
        String balanceCalcResult = timeHandler.formatDuration(balanceCalcResultDuration);

        return new TodayReport(todayTracks, estimatedEnd, balanceCalcFtDate, today, balanceCalcResult);
    }

    public String calculateBalance(DatePeriod datePeriod) {
        validator.validateCalculateBalance(datePeriod);
        LocalDate ftDate = datePeriod.getFtDate().toLocalDate();
        LocalDate ltDate = datePeriod.getLtDate().toLocalDate();

        Duration balanceCalcResultDuration = calculateBalance(ftDate, ltDate);

        return timeHandler.formatDuration(balanceCalcResultDuration);
    }

    public BalanceReport getActualBalanceReport() {
        LocalDate dayAtInitialTime = intranetDao.getDayAtInitialTime(context.getUsername());
        Duration initialTime = intranetDao.getInitialTime(context.getUsername());
        LocalDate today = LocalDate.now();
        Duration balance = calculateBalance(dayAtInitialTime.plusDays(1), today);
        balance = balance.plus(initialTime);

        ZonedDateTime initialDay = dayAtInitialTime.atStartOfDay(ZoneId.systemDefault());
        String initialBalance = timeHandler.formatDuration(initialTime);
        String actualBalance = timeHandler.formatDuration(balance);

        return new BalanceReport(initialDay, initialBalance, actualBalance);
    }

    private Duration calculateBalance(LocalDate ftDate, LocalDate ltDate) {
        LocalDate firstLocalDay = intranetDao.getFirstDay(context.getUsername());

        if (ftDate.isBefore(firstLocalDay)) {
            downloadMonths((int) ChronoUnit.MONTHS.between(ftDate, firstLocalDay)+1);
        }

        Set<DailyTimeReport> dailyReports = intranetDao.getDailyReports(context.getUsername());

        return timeHandler.calculateBalance(ftDate, ltDate, dailyReports);
    }
}
