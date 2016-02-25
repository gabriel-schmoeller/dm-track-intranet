package ponto.intranet.service.domain;

import java.time.ZonedDateTime;

/**
 * @author gabriel.schmoeller
 */
public class BalanceReport {

    private ZonedDateTime initialDay;
    private String initialBalance;
    private String actualBalance;

    public BalanceReport() {
    }

    public BalanceReport(ZonedDateTime initialDay, String initialBalance, String actualBalance) {
        this.initialDay = initialDay;
        this.initialBalance = initialBalance;
        this.actualBalance = actualBalance;
    }

    public ZonedDateTime getInitialDay() {
        return initialDay;
    }

    public String getInitialBalance() {
        return initialBalance;
    }

    public String getActualBalance() {
        return actualBalance;
    }
}
