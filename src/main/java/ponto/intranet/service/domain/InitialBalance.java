package ponto.intranet.service.domain;

import java.time.ZonedDateTime;

/**
 * @author gabriel.schmoeller
 */
public class InitialBalance {

    private ZonedDateTime initialDate;
    private String initialBalance;

    public InitialBalance() {
    }

    public InitialBalance(ZonedDateTime initialDate, String initialBalance) {
        this.initialDate = initialDate;
        this.initialBalance = initialBalance;
    }

    public ZonedDateTime getInitialDate() {
        return initialDate;
    }

    public String getInitialBalance() {
        return initialBalance;
    }
}
