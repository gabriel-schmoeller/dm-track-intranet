package ponto.intranet.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import ponto.intranet.service.domain.Ticket;
import ponto.intranet.service.domain.TicketType;
import ponto.intranet.persistence.domain.TimeTracking;

/**
 * @author gabriel.schmoeller
 */
@Component
public class IntranetParser {

    private static final String USER_NAME_FIELD = "ctl00$usrLogin$UserName";
    private static final String PASSWORD_FIELD = "ctl00$usrLogin$Password";

    private static final String DESCRIPTION_FIELD = "ctl00$placeHolder$pagectrlChamado$DES_CHA";
    private static final String DETAILS_FIELD = "ctl00$placeHolder$pagectrlChamado$TXT_CLIENTE_CHA";
    private static final String DEPARTMENT_FIELD = "ctl00$placeHolder$pagectrlChamado$cboSEQ_TIPO_CATCHA";
    private static final String CATEGORY_FIELD = "ctl00$placeHolder$pagectrlChamado$cboSEQ_CATCHA";
    private static final String PRIORITY_FIELD = "ctl00$placeHolder$pagectrlChamado$ID_PRICHA";
    private static final String EXPECT_TIME_FIELD = "ctl00$placeHolder$pagectrlChamado$SEQ_TEMPO";
    private static final String ASYNCPOST_FIELD = "__ASYNCPOST";
    private static final String DXSCRIPT_FIELD = "DXScript";
    private static final String SCRIPT_MANAGER_FIELD = "ctl00$ScriptManager1";
    private static final String EVENTTARGET_FIELD = "__EVENTTARGET";
    private static final String VIEWSTATE_FIELD = "__VIEWSTATE";
    private static final String EVENTVALIDATION_FIELD = "__EVENTVALIDATION";

    public Document parseDocument(InputStream content, String baseUri) throws IOException {
        Document parse = Jsoup.parse(content, "UTF-8", baseUri);
        parse.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        return parse;
    }

    public List<NameValuePair> parseLoginFields(Document document, String username, String password) {
        List<NameValuePair> fields = new ArrayList<>();

        Elements elements = document.select("#aspnetForm input");
        for (Element element : elements) {
            String name = element.attr("name");
            String value = element.attr("value");

            if (!name.isEmpty()) {
                if (USER_NAME_FIELD.equals(name)) {
                    value = username;
                } else if (PASSWORD_FIELD.equals(name)) {
                    value = password;
                }

                fields.add(new BasicNameValuePair(name, value));
            }
        }

        fields.add(new BasicNameValuePair("ctl00$usrLogin$LoginImageButton.x", "8"));
        fields.add(new BasicNameValuePair("ctl00$usrLogin$LoginImageButton.y", "11"));

        return fields;
    }

    public Map<String, String> parseTicketFields(Document document, int code1) {
        Elements elements = document.select("input");
        Map<String, String> fields = elements.stream().filter(e -> !e.attr("name").isEmpty())
                .collect(Collectors.toMap(e -> e.attr("name"), e -> e.attr("value")));

        fields.put(DEPARTMENT_FIELD, String.valueOf(code1));
        fields.put(ASYNCPOST_FIELD, "true");
        fields.put(DXSCRIPT_FIELD, "1_32,1_61,1_62,1_59,2_22,2_29,2_28,2_15");
        fields.put(SCRIPT_MANAGER_FIELD, "ctl00$placeHolder$pagectrlChamado$UpdatePanel2|ctl00_placeHolder_pagectrlChamado_UpdatePanel2");
        fields.put(EVENTTARGET_FIELD, "ctl00_placeHolder_pagectrlChamado_UpdatePanel2");
        fields.put(DETAILS_FIELD, "");
        fields.put(DESCRIPTION_FIELD+"$CVS", "");
        fields.put("ctl00$hfDisplayResolution", "1920x1080");
        fields.put("", "");

        fields.remove("ctl00$placeHolder$btnComamnd");
        fields.remove("ctl00$placeHolder$btnVoltar");

        return fields;
    }

    public Map<String, String> parseTicketFields(Document document, Map<String, String> fields, int code1, int code2) {
        String html = document.html();

        fields.put(CATEGORY_FIELD, String.valueOf(code2));

        Matcher matcher = Pattern.compile(VIEWSTATE_FIELD + "\\|.*?\\|").matcher(html);
        matcher.find();
        fields.put(VIEWSTATE_FIELD, matcher.group().split("\\|")[1]);

        matcher = Pattern.compile(EVENTVALIDATION_FIELD + "\\|.*?\\|").matcher(html);
        matcher.find();
        fields.put(EVENTVALIDATION_FIELD, matcher.group().split("\\|")[1]);

        return fields;
    }

    public List<NameValuePair> parseTicketFields(Document document, Map<String, String> fields, Ticket ticket) {
        String html = document.html();

        Matcher matcher = Pattern.compile(VIEWSTATE_FIELD + "\\|.*?\\|").matcher(html);
        matcher.find();
        fields.put(VIEWSTATE_FIELD, matcher.group().split("\\|")[1]);

        matcher = Pattern.compile(EVENTVALIDATION_FIELD + "\\|.*?\\|").matcher(html);
        matcher.find();
        fields.put(EVENTVALIDATION_FIELD, matcher.group().split("\\|")[1]);

        TicketType type = ticket.getType();

        fields.put(SCRIPT_MANAGER_FIELD, "ctl00$placeHolder$UpdatePanelPage|ctl00$placeHolder$btnComamnd");
        fields.put(EVENTTARGET_FIELD, "");

        fields.put(DESCRIPTION_FIELD, ticket.getDescription());
        fields.put(DETAILS_FIELD, ticket.getDetails());
        fields.put(DETAILS_FIELD + "$CVS", "");
        fields.put(DEPARTMENT_FIELD, String.valueOf(type.getDepartment()));
        fields.put(CATEGORY_FIELD, String.valueOf(type.getCategory()));
        fields.put("ctl00$placeHolder$btnComamnd", "");

        fields.remove("");

        if (type.hasPriority()) {
            fields.put(PRIORITY_FIELD, String.valueOf(ticket.getPriority()));
        }
        if (type.hasExpectTime()) {
            fields.put(EXPECT_TIME_FIELD, String.valueOf(ticket.getExpectTime()));
        }

        return fieldsToList(fields);
    }

    public List<NameValuePair> fieldsToList(Map<String, String> fields) {
        return fields.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<TimeTracking> parseFullTimeTracking(Document document) {
        List<TimeTracking> timesTracking = new ArrayList<>();

        Matcher matcher = Pattern.compile("\\d+/\\d+/\\d+").matcher(document.select(".data:eq(1)").html());
        matcher.find();
        LocalDate date = LocalDate.parse(matcher.group().trim(), DateTimeFormatter.ofPattern("dd/MM/yy"));
        LocalDate now = LocalDate.now();

        for (Element element : document.select(".thCabecalhoDia")) {
            if (date.isAfter(now)) {
                date = date.plusDays(1);
                continue;
            }

            String report = element.select(".registros").html();
            String[] pieces = report.split("--+");
            List<LocalTime> tracks = new ArrayList<>();
            List<LocalTime> justified = new ArrayList<>();

            for (int i = 0; i < pieces.length; i++) {
                Collection<LocalTime> times = extractTimes(pieces[i]);
                switch (i) {
                    case 1:
                        tracks.addAll(times);
                        break;
                    case 2:
                        justified.addAll(times);
                        break;
                }
            }

            timesTracking.add(new TimeTracking(date, tracks, justified, isFreeDay(date, element), isIncomplete(tracks)));
            date = date.plusDays(1);
        }

        return timesTracking;
    }

    public Integer extractTicketNo(Document document) {
        return Integer.valueOf(document.select("#ctl00_placeHolder_pagectrlChamado_FormChamado_SEQ_CHA").html());
    }

    public List<String> parseUsersToLink(Document document) {
        return document.select("#ctl00_placeHolder_pagectrlChamado_lsbUsuarios option").stream()
                .map(Element::val).collect(Collectors.toList());
    }

    public List<NameValuePair> parseLinkUsersFields(Document document, List<String> linkedUsers) {
        Map<String, String> map = document.select("input").stream()
                .filter(e -> !e.attr("name").isEmpty())
                .collect(Collectors.toMap(e -> e.attr("name"), Element::val));

        map.remove("ctl00$placeHolder$pagectrlChamado$btSelectUsuario");
        map.remove("ctl00$placeHolder$pagectrlChamado$btUnselectUsuario");
        map.remove("ctl00$placeHolder$pagectrlChamado$btSelectEmail");
        map.remove("ctl00$placeHolder$pagectrlChamado$btUnselectEmail");
        map.remove("ctl00$placeHolder$pagectrlChamado$btSelectChamado");
        map.remove("ctl00$placeHolder$pagectrlChamado$btUnselectChamado");
        map.remove("ctl00$placeHolder$pagectrlChamado$btSelectChamado");
        map.remove("ctl00$placeHolder$btnVoltar");

        map.put("ctl00$ScriptManager1", "ctl00$placeHolder$UpdatePanelPage|ctl00$placeHolder$btnComamnd");
        map.put("DXScript", "1_32,1_61,1_62,1_59,2_15");

        List<NameValuePair> fields = map.entrySet().stream().map(
                e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList());

        fields.addAll(linkedUsers.stream()
                .map(u -> new BasicNameValuePair("ctl00$placeHolder$pagectrlChamado$lsbUsuariosChamado", u))
                .collect(Collectors.toList()));

        return fields;
    }

    public List<String> parseSuccessLinkedUsers(Document document) {
        return document.select("#ctl00_placeHolder_pagectrlChamado_lsbUsuariosChamado option").stream()
                .map(Element::val).collect(Collectors.toList());
    }

    private boolean isIncomplete(List<LocalTime> tracks) {
        return tracks.size() % 2 != 0;
    }

    private boolean isFreeDay(LocalDate date, Element element) {
        String eClass = element.attr("class");
        String eStyle = element.attr("style");

        boolean folga = eClass.equals("feriasFeriado") || eStyle.contains("#C77183");
        boolean weekend = date.getDayOfWeek() == DayOfWeek.SUNDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY;

        return weekend || folga;
    }

    private Collection<LocalTime> extractTimes(String piece) {
        List<LocalTime> times = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+:\\d+").matcher(piece);

        while (matcher.find()) {
            times.add(LocalTime.parse(matcher.group(), DateTimeFormatter.ofPattern("HH:mm")));
        }

        return times;
    }
}
