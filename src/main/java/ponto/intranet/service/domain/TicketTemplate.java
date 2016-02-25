package ponto.intranet.service.domain;

/**
 * @author gabriel.schmoeller
 */
public class TicketTemplate {

    private String name;
    private TicketType type;
    private String description;
    private String details;
    private int priority;
    private int expectTime;
    private boolean hasPriority;
    private boolean hasExpectedTime;

    public TicketTemplate(String name, TicketType type, String description, String details, int priority, int expectTime) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.details = details;
        this.priority = priority;
        this.expectTime = expectTime;
        this.hasPriority = type.hasPriority();
        this.hasExpectedTime = type.hasExpectTime();
    }

    public String getName() {
        return name;
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

    public boolean getHasPriority() {
        return hasPriority;
    }

    public boolean getHasExpectedTime() {
        return hasExpectedTime;
    }
}
