package ponto.intranet.service.domain;

import java.util.List;

/**
 * @author gabriel.schmoeller
 */
public class MonthReport implements Comparable<MonthReport> {

    private int month;
    private String balance;
    private List<DailyReport> dailyReports;

    public MonthReport() {
    }

    public MonthReport(int month, String balance, List<DailyReport> dailyReports) {
        this.month = month;
        this.balance = balance;
        this.dailyReports = dailyReports;
    }

    public int getMonth() {
        return month;
    }

    public String getBalance() {
        return balance;
    }

    public List<DailyReport> getDailyReports() {
        return dailyReports;
    }

    @Override
    public int compareTo(MonthReport o) {
        return month - o.getMonth();
    }
}
