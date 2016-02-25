package ponto.intranet.service;

import static ponto.intranet.service.domain.TicketType.RH_ACESSO_FIM_SEMANA;
import static ponto.intranet.service.domain.TicketType.RH_PONTO_ESQUECIMENTO;
import static ponto.intranet.service.domain.TicketType.RH_PONTO_OUTROS;
import static ponto.intranet.service.domain.TicketType.RH_PONTO_TRABALHO_EXTERNO;
import static ponto.intranet.service.domain.TicketType.TI_VPN_TEMPORARIA;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ponto.intranet.service.domain.TicketType;
import ponto.intranet.service.domain.TicketTemplate;

/**
 * @author gabriel.schmoeller
 */
@Component
public class TicketTypeTemplateFactory {

    public List<TicketTemplate> create() {
        List<TicketTemplate> templates = new ArrayList<>();

        templates.add(createForRhAcessoFimSemana());
        templates.add(createForTiVpnTemporaria());
        templates.add(createForRhPontoTrabalhoExterno());
        templates.add(createForRhPontoEsquecimento());
        templates.add(createForRhPontoOutros());

        return templates;
    }

    private TicketTemplate createForRhPontoEsquecimento() {
        String typeName = "RH - Correção de ponto por esquecimento";
        TicketType type = RH_PONTO_ESQUECIMENTO;
        String description = "Esqueci de bater o ponto no dia {date}";
        String details = "Favor preencher o ponto das 00:00";
        int priority = 12;
        int expectTime = 2;

        return new TicketTemplate(typeName, type, description, details, priority, expectTime);
    }

    private TicketTemplate createForRhPontoTrabalhoExterno() {
        String typeName = "RH - Correção de ponto por trabalho externo";
        TicketType type = RH_PONTO_TRABALHO_EXTERNO;
        String description = "Trabalho externo dia {date}";
        String details = "Horário dos pontos: 8:00 - 12:00 - 13:00 - 17:30";
        int priority = 12;
        int expectTime = 2;

        return new TicketTemplate(typeName, type, description, details, priority, expectTime);
    }

    private TicketTemplate createForRhPontoOutros() {
        String typeName = "RH - Correção de ponto por outros motivos";
        TicketType type = RH_PONTO_OUTROS;
        String description = "Corrigir o ponto do dia {date}";
        String details = "Horário dos pontos: 8:00 - 12:00 - 13:00 - 17:30";
        int priority = 12;
        int expectTime = 2;

        return new TicketTemplate(typeName, type, description, details, priority, expectTime);
    }

    private TicketTemplate createForRhAcessoFimSemana() {
        String typeName = "RH - Acesso para fim de semana";
        TicketType type = RH_ACESSO_FIM_SEMANA;
        String description = "Acesso para o dia {date}";
        String details = "Data: {date}\n" +
                "Localização do posto de trabalho: Prédio administrativo\n" +
                "Horário de início: 8:00\n" +
                "Horário de término: 18:00\n" +
                "Finalidade: Trabalhar";
        int priority = 10;
        int expectTime = 2;

        return new TicketTemplate(typeName, type, description, details, priority, expectTime);
    }

    private TicketTemplate createForTiVpnTemporaria() {
        String typeName = "TI - Acesso a VPN temporária";
        TicketType type = TI_VPN_TEMPORARIA;
        String description = "Solicito acesso a VPN para o dia {date}";
        String details = "Sistema Operacional: Windows \n" +
                "Serviço/Servidor: jabber, bugzilla, ticketzilla, Rally, ger-redes.datacom.net, servidor.datacom, testlink.datacom/testlink/, git \n" +
                "Data: {date}";
        int priority = 12;
        int expectTime = 2;

        return new TicketTemplate(typeName, type, description, details, priority, expectTime);
    }
}
