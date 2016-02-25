package ponto.intranet.service.domain;

/**
 * @author gabriel.schmoeller
 */
public class Ticket {

    private TicketType type;
    private String description;
    private String details;
    private int priority;
    private int expectTime;

    public Ticket() {
    }

    public Ticket(TicketType type, String description, String details, int priority, int expectTime, char projOperFlag) {
        this.type = type;
        this.description = description;
        this.details = details;
        this.priority = priority;
        this.expectTime = expectTime;
    }

    public TicketType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getDetails() {
        return details;
    }

    public int getPriority() {
        return priority;
    }

    public int getExpectTime() {
        return expectTime;
    }
}
