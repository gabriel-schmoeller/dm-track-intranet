package ponto.intranet.service.domain;

/**
 * @author gabriel.schmoeller
 */
public enum TicketType {

    RH_PONTO_ESQUECIMENTO(17, 1961, true, false),
    RH_PONTO_OUTROS(17, 1342, true, false),
    RH_PONTO_TRABALHO_EXTERNO(17, 1093, true, false),
    RH_ACESSO_FIM_SEMANA(17, 1088, true, false),
    TI_VPN_TEMPORARIA(2, 3748, false, true);

    private final int department;
    private final int category;
    private final boolean priority;
    private final boolean expectTime;

    TicketType(int department, int category, boolean priority, boolean expectTime) {
        this.department = department;
        this.category = category;
        this.priority = priority;
        this.expectTime = expectTime;
    }

    public int getDepartment() {
        return department;
    }

    public int getCategory() {
        return category;
    }

    public boolean hasPriority() {
        return priority;
    }

    public boolean hasExpectTime() {
        return expectTime;
    }
}
