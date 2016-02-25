package ponto.intranet.service.domain;

import java.time.ZonedDateTime;

/**
 * @author gabriel.schmoeller
 */
public class DatePeriod {

    private ZonedDateTime ftDate;
    private ZonedDateTime ltDate;

    public DatePeriod() {
    }

    public DatePeriod(ZonedDateTime ftDate, ZonedDateTime ltDate) {
        this.ftDate = ftDate;
        this.ltDate = ltDate;
    }

    public ZonedDateTime getFtDate() {
        return ftDate;
    }

    public ZonedDateTime getLtDate() {
        return ltDate;
    }
}
