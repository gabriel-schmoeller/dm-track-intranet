package ponto.intranet.service.domain;

import java.util.List;

/**
 * @author gabriel.schmoeller
 */
public class TicketsAndLinks {

    private int ticketNo;
    private List<String> usersLink;

    public TicketsAndLinks() {
    }

    public TicketsAndLinks(int ticketNo, List<String> usersLink) {
        this.ticketNo = ticketNo;
        this.usersLink = usersLink;
    }

    public int getTicketNo() {
        return ticketNo;
    }

    public List<String> getUsersLink() {
        return usersLink;
    }
}
