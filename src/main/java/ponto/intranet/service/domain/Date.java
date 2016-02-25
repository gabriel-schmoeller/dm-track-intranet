package ponto.intranet.service.domain;

import java.time.ZonedDateTime;

/**
 * @author gabriel.schmoeller
 */
public class Date {

    private ZonedDateTime date;

    public Date() {
    }

    public Date(ZonedDateTime date) {
        this.date = date;
    }

    public ZonedDateTime getDate() {
        return date;
    }
}
