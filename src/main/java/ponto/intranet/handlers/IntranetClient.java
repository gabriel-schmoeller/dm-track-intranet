package ponto.intranet.handlers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ponto.intranet.service.domain.TicketsAndLinks;
import ponto.intranet.handlers.domain.SessionContext;
import ponto.intranet.service.domain.Ticket;
import ponto.intranet.persistence.domain.TimeTracking;
import ponto.intranet.exception.IntranetParserException;
import ponto.intranet.exception.LoginCredentialsException;

/**
 * @author gabriel.schmoeller
 */
@Component
public class IntranetClient {

    private static final String INDEX = "/Default.aspx";
    private static final String BASE = "https://intranet.dm";
    private static final String PROJETO_HORA = "/projeto_ped/ConCadSubProjetoHora.aspx";
    private static final String CHAMADO = "/chamado/CadChamadoDados.aspx";
    private static final String CHAMADO_VINCULO = "/chamado/CadChamadoVinculo.aspx?SEQ_CHA=%s&MODE=VINCULO";
    private static final String PROJETO_HORA_DATA1 = "DATA1";
    private static final String PROJETO_HORA_DATA2 = "DATA2";
    private static final String PROJETO_HORA_USERNAME = "USERNAME";
    private static final String LOGOFF = "/Logoff.aspx";
    private final CloseableHttpClient httpClient;
    private final IntranetParser parser;

    @Autowired
    public IntranetClient(IntranetParser parser) {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).useProtocol("TLS").build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        httpClient = HttpClientBuilder
                .create()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setSSLContext(sslContext)
                .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36")
                .build();

        this.parser = parser;
    }

    public void logout(SessionContext context) {
        HttpGet httpGet = new HttpGet(BASE + LOGOFF);

        try {
            execute(httpGet, context.getHttpContext()).close();
            context.setUsername("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String username, String password, SessionContext context) throws LoginCredentialsException {
        if (hasActiveLogin(context)) {
            logout(context);
        }

        List<NameValuePair> fields = getOrRetry(
                        (anyway) -> executeAndParse(context, new HttpGet(BASE + INDEX)),
                        (document) -> parser.parseLoginFields(document.get(), username, password));

        HttpPost login = new HttpPost(BASE + INDEX);
        login.setEntity(new UrlEncodedFormEntity(fields, StandardCharsets.UTF_8));

        Document loginPage = getOrRetry((anyway) -> executeAndParse(context, login), Optional::get);

        if (!hasActiveLogin(context)) {
            if (!loginPage.select(".login_erro").isEmpty()) {
                throw new LoginCredentialsException(loginPage.select(".login_erro").html());
            } else {
                throw new IntranetParserException("Falha desconhecida no login, tente novamente ou desista.");
            }
        }

        context.setUsername(username);
    }

    public List<TimeTracking> getDates(SessionContext context, List<LocalDate> dates) {
        Set<Integer> weeks = getWeeksToTrack(dates);

        return downloadWeeks(context, weeks);
    }

    public List<TimeTracking> getPeriod(LocalDate stDate, LocalDate edDate, SessionContext context) {
        Set<Integer> weeksToTrack = getWeeksToTrack(stDate, edDate);
        List<TimeTracking> timeTrackings = downloadWeeks(context, weeksToTrack);
        Iterator<TimeTracking> iter = timeTrackings.iterator();

        while (iter.hasNext()) {
            LocalDate date = iter.next().getDate();
            if (date.isBefore(stDate) || date.isAfter(edDate)) {
                iter.remove();
            }
        }

        return timeTrackings;
    }

    public TimeTracking getDay(LocalDate date, SessionContext context) {
        return getPeriod(date, date, context).get(0);
    }

    public Integer openTicket(Ticket ticket, SessionContext context) {
        Map<String, String> fields = getOrRetry(
                (anyway) -> executeAndParse(context, new HttpGet(BASE + CHAMADO)),
                (document) -> parser.parseTicketFields(document.get(), ticket.getType().getDepartment()));

        HttpPost getFields = new HttpPost(BASE + CHAMADO);
        getFields.setEntity(new UrlEncodedFormEntity(parser.fieldsToList(fields), StandardCharsets.UTF_8));

        Map<String, String> fields2 = getOrRetry(
                (anyway) -> executeAndParse(context, getFields),
                (document) -> parser.parseTicketFields(document.get(), fields, ticket.getType().getDepartment(), ticket.getType()
                        .getCategory()));

        HttpPost getFields2 = new HttpPost(BASE + CHAMADO);
        getFields2.setEntity(new UrlEncodedFormEntity(parser.fieldsToList(fields2), StandardCharsets.UTF_8));

        List<NameValuePair> fieldsParsed = getOrRetry(
                (anyway) -> executeAndParse(context, getFields2),
                (document) -> parser.parseTicketFields(document.get(), fields2, ticket));

        HttpPost chamado = new HttpPost(BASE + CHAMADO);
        chamado.setEntity(new UrlEncodedFormEntity(fieldsParsed, StandardCharsets.UTF_8));

        return getOrRetry(anyway -> executeAndParse(context, chamado), document -> parser.extractTicketNo(document.get()));
    }

    public List<String> linksToTicket(int ticketNo, SessionContext context) {
        List<String> users = getOrRetry(
                (anyway) -> executeAndParse(context, new HttpGet(BASE + String.format(CHAMADO_VINCULO, ticketNo))),
                (document) -> parser.parseUsersToLink(document.get()));

        users.removeIf(String::isEmpty);

        return users.stream().map(s -> s.replace(".", " ")).collect(Collectors.toList());
    }

    public boolean linkAtTicket(TicketsAndLinks ticketsAndLinks, SessionContext context) {
        List<String> linkedUsers = ticketsAndLinks.getUsersLink().stream().map(s -> s.replace(" ", ".")).collect(Collectors.toList());

        List<NameValuePair> linkUsersFields = getOrRetry(
                (anyway) -> executeAndParse(context, new HttpGet(BASE + String.format(CHAMADO_VINCULO, ticketsAndLinks.getTicketNo()))),
                (document) -> parser.parseLinkUsersFields(document.get(), linkedUsers));

        HttpPost linkAtTicket = new HttpPost(BASE + String.format(CHAMADO_VINCULO, ticketsAndLinks.getTicketNo()));
        linkAtTicket.setEntity(new UrlEncodedFormEntity(linkUsersFields, StandardCharsets.UTF_8));

        List<String> succesLinked = getOrRetry(
                anyway -> executeAndParse(context, linkAtTicket),
                document -> parser.parseSuccessLinkedUsers(document.get()));

        return succesLinked.containsAll(linkedUsers);
    }

    private List<TimeTracking> downloadWeeks(SessionContext context, Set<Integer> weeksToTrack) {
        List<TimeTracking> timesTracking = new ArrayList<>();
        String username = context.getUsername().toUpperCase();

        for (Integer pages : weeksToTrack) {
            try {
                URI uri = new URIBuilder(BASE + PROJETO_HORA)
                        .addParameter(PROJETO_HORA_DATA1, String.valueOf(pages + 1))
                        .addParameter(PROJETO_HORA_DATA2, String.valueOf(pages))
                        .addParameter(PROJETO_HORA_USERNAME, username).build();

                List<TimeTracking> timeTrackings = getOrRetry(
                        (anyway) -> executeAndParse(context, new HttpGet(uri)),
                        (document) -> parser.parseFullTimeTracking(document.get()));

                timesTracking.addAll(timeTrackings);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(timesTracking);

        return timesTracking;
    }

    private Set<Integer> getWeeksToTrack(List<LocalDate> dates) {
        Set<Integer> pages = new HashSet<>();
        LocalDate now = getFirstDayOfWeek(LocalDate.now());
        LocalDate ftDayOfWeek;

        for (LocalDate date : dates) {
            ftDayOfWeek = getFirstDayOfWeek(date);
            pages.add((int) ChronoUnit.WEEKS.between(ftDayOfWeek, now));
        }

        return pages;
    }

    private Set<Integer> getWeeksToTrack(LocalDate stDate, LocalDate edDate) {
        Set<Integer> pages = new HashSet<>();
        LocalDate now = LocalDate.now();
        LocalDate ftDayOfWeek = getFirstDayOfWeek(now);

        stDate = getFirstDayOfWeek(stDate);
        edDate = getFirstDayOfWeek(edDate);

        int stIdx = (int) ChronoUnit.WEEKS.between(edDate, ftDayOfWeek);
        int edIdx = stIdx + (int) ChronoUnit.WEEKS.between(stDate, edDate);

        for (int i = edIdx; i >= stIdx; i--) {
            pages.add(i);
        }

        return pages;
    }

    private LocalDate getFirstDayOfWeek(LocalDate now) {
        int dayOfWeek = now.getDayOfWeek().getValue();
        return now.minusDays((dayOfWeek < 7) ? dayOfWeek : 0);
    }

    private boolean hasActiveLogin(SessionContext context) {
        return context.getHttpContext().getCookieStore().getCookies().stream().
                anyMatch(cookie -> cookie.getName().equals("intranet_user"));
    }

    private <T> T getOrRetry(TryAccessIntranet<?, Document> execute, TryAccessIntranet<Document, T> documentParser) throws IntranetParserException {
        IntranetParserException exception = null;
        Document document = null;

        for (int i = 0; i < 3; i++) {
            try {
                document = execute.tryDo(Optional.empty());
            } catch (IOException e) {
                exception = new IntranetParserException("Erro ao conectar com a intranet, tente novamente ou desista", e);
            }

            if (document != null) {
                try {
                    if (!document.select("#ctl00_placeHolder_PanelErro").isEmpty()) {
                        exception = new IntranetParserException("Erro ao acessar a intranet: " +
                                document.select(".error_msg").html());
                        continue;
                    }

                    return documentParser.tryDo(Optional.of(document));
                } catch (Exception e) {
                    try {
                        Files.write(Paths.get("failures/" + UUID.randomUUID().toString() + ".html"), document.html().getBytes());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    exception = new IntranetParserException("Formato de HTML desconhecido retornado pela intranet, " +
                            "tente novamente ou desista", e);
                }
            }
        }

        throw exception;
    }

    private Document executeAndParse(SessionContext context, HttpUriRequest request) throws IOException {
        CloseableHttpResponse response = null;
        try {
            response = execute(request, context.getHttpContext());

            Document document = parser.parseDocument(response.getEntity().getContent(), BASE);
            response.close();

            return document;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private CloseableHttpResponse execute(HttpUriRequest request, HttpClientContext httpContext) throws IOException {
        return httpClient.execute(request, httpContext);
    }
}
