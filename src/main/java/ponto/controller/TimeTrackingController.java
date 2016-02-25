package ponto.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ponto.intranet.service.domain.Ticket;
import ponto.intranet.exception.ErrorException;
import ponto.intranet.exception.LoginCredentialsException;
import ponto.intranet.exception.ValidationException;
import ponto.intranet.service.IntranetService;
import ponto.intranet.service.domain.TimeContract;
import ponto.intranet.service.domain.BalanceReport;
import ponto.intranet.service.domain.DailyReport;
import ponto.intranet.service.domain.Date;
import ponto.intranet.service.domain.DatePeriod;
import ponto.intranet.service.domain.InitialBalance;
import ponto.intranet.service.domain.LocalData;
import ponto.intranet.service.domain.MonthReport;
import ponto.intranet.service.domain.TicketTemplate;
import ponto.intranet.service.domain.TicketsAndLinks;
import ponto.intranet.service.domain.TodayReport;
import ponto.intranet.service.domain.UserCredentials;

/**
 * @author gabriel.schmoeller
 */
@RestController
public class TimeTrackingController {

    private final IntranetService intranetService;

    @Autowired
    public TimeTrackingController(IntranetService intranetService) {
        this.intranetService = intranetService;
    }

    @RequestMapping(value = "/service/login", method = RequestMethod.POST)
    public Map<String, String> login(@RequestBody UserCredentials user, HttpServletRequest request) throws LoginCredentialsException {
        intranetService.login(user);
        request.getSession().setMaxInactiveInterval(604800);

        return Collections.singletonMap("username", user.getUser());
    }

    @RequestMapping("/service/logged")
    public Map<String, String> logged() {
        return Collections.singletonMap("username", intranetService.login());
    }

    @RequestMapping("/service/logout")
    public void logout(HttpServletRequest request) {
        intranetService.logout();
        request.getSession().invalidate();
    }

    @RequestMapping("/service/download-months/{months}")
    public LocalData downloadMonths(@PathVariable int months) {
        return intranetService.downloadMonths(months);
    }

    @RequestMapping("/service/download-months")
    public LocalData downloadMonths() {
        return intranetService.getLocalData();
    }

    @RequestMapping("/service/today-report")
    public TodayReport todayReport() {
        return intranetService.getTodayReport();
    }

    @RequestMapping("/service/time-contract")
    public List<TimeContract> timeContracts() {
        return intranetService.getTimeContract();
    }

    @RequestMapping("/service/update-date")
    public Map<String, Boolean> updateDate(@RequestBody Date date) {
        return Collections.singletonMap("hasChange", intranetService.updateDay(date));
    }

    @RequestMapping("/service/mark-incomplete")
    public void markAsIncomplete(@RequestBody Date date) {
        intranetService.markAsIncomplete(date);
    }

    @RequestMapping("/service/set-note")
    public void addNote(@RequestBody DailyReport report) {
        intranetService.setNote(report);
    }

    @RequestMapping("/service/mark-complete")
    public void markAsComplete(@RequestBody Date date) {
        intranetService.marksAsComplete(date);
    }

    @RequestMapping(value = "/service/calculate-balance", method = RequestMethod.POST)
    public Map<String, String> calculateBalance(@RequestBody DatePeriod datePeriod) {
        return Collections.singletonMap("result", intranetService.calculateBalance(datePeriod));
    }

    @RequestMapping("/service/actual-balance")
    public BalanceReport actualBalance() {
        return intranetService.getActualBalanceReport();
    }

    @RequestMapping(value = "/service/initial-balance", method = RequestMethod.POST)
    public BalanceReport updateActualInitialBalance(@RequestBody InitialBalance initialBalance) {
        intranetService.setInitialBalance(initialBalance);

        return intranetService.getActualBalanceReport();
    }

    @RequestMapping(value = "/service/open-ticket", method = RequestMethod.POST)
    public TicketsAndLinks openTicket(@RequestBody Ticket ticket) {
        return intranetService.openTicket(ticket);
    }

    @RequestMapping(value = "/service/link-at-ticket", method = RequestMethod.POST)
    public Map<String, Boolean> linkAtTicket(@RequestBody TicketsAndLinks ticket) {
        return Collections.singletonMap("success", intranetService.linkAtTicket(ticket));
    }

    @RequestMapping("/service/daily-reports")
    public List<DailyReport> dailyReports() {
        return intranetService.getDailyReports();
    }

    @RequestMapping("/service/daily-reports/{year}")
    public List<MonthReport> monthReports(@PathVariable int year) {
        return intranetService.getMonthReport(year);
    }

    @RequestMapping("/service/ticket-types")
    public List<TicketTemplate> getTicketTypes() {
        return intranetService.getTicketTypes();
    }

    /////// Error handlers ///////
    @ExceptionHandler(ErrorException.class)
    public void errorExceptionHandler(HttpServletResponse response, Exception exception) throws IOException {
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());
        exception.printStackTrace();
    }

    @ExceptionHandler(ValidationException.class)
    public void errorValidationHandler(HttpServletResponse response, Exception exception) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }
}
