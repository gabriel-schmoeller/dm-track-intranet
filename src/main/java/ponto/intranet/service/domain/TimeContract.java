package ponto.intranet.service.domain;

import java.time.ZonedDateTime;

/**
 * @author gabriel.schmoeller
 */
public class TimeContract {

    private ZonedDateTime ftDate;
    private ZonedDateTime ltDate;
    private String time;

    public TimeContract() {
    }

    public TimeContract(ZonedDateTime ftDate, ZonedDateTime ltDate, String time) {
        this.ftDate = ftDate;
        this.ltDate = ltDate;
        this.time = time;
    }

    public ZonedDateTime getFtDate() {
        return ftDate;
    }

    public ZonedDateTime getLtDate() {
        return ltDate;
    }

    public String getTime() {
        return time;
    }
}
