package ponto.intranet.service.domain;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author gabriel.schmoeller
 */
public class LocalData {

    private ZonedDateTime ftLocalDate;
    private List<Integer> localYears;

    public LocalData() {
    }

    public LocalData(ZonedDateTime ftLocalDate, List<Integer> localYears) {
        this.ftLocalDate = ftLocalDate;
        this.localYears = localYears;
    }

    public ZonedDateTime getFtLocalDate() {
        return ftLocalDate;
    }

    public List<Integer> getLocalYears() {
        return localYears;
    }
}
