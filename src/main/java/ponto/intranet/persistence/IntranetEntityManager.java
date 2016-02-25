package ponto.intranet.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ponto.intranet.persistence.domain.DailyTimeReport;
import ponto.intranet.persistence.domain.TimeByDay;
import ponto.intranet.persistence.domain.IntranetEntity;

/**
 * @author gabriel.schmoeller
 */
@Component
public class IntranetEntityManager {

    private static final String FILE_SUFIX = "_ponto.json";
    private static final String FILE_SUFIX_TMP = "_ponto.json.tmp";
    private final ObjectMapper objectMapper;

    @Autowired
    public IntranetEntityManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public IntranetEntity getEntity(String username) {
        IntranetEntity intranetEntity;

        if (existsFile(username)) {
            intranetEntity = load(username);
        } else {
            intranetEntity = buildDefault(username);
            persist(intranetEntity);
        }

        return intranetEntity;
    }

    public void persist(IntranetEntity intranetEntity) {
        try {
            Path saveFile = getPath(intranetEntity.getUsername());
            Path tempFile = getTmpPath(intranetEntity.getUsername());

            if (Files.exists(saveFile)) {
                Files.move(saveFile, tempFile);
            }

            objectMapper.writeValue(saveFile.toFile(), intranetEntity);
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean existsFile(String username) {
        Path savePath = getPath(username);
        Path tempPath = getTmpPath(username);

        return Files.exists(savePath) || Files.exists(tempPath);
    }

    private IntranetEntity load(String username) {
        try {
            Path savePath = getPath(username);
            Path tmpPath = getTmpPath(username);

            if (!Files.exists(savePath) && Files.exists(tmpPath)) {
                Files.move(savePath, tmpPath);
            }

            return objectMapper.readValue(savePath.toFile(), IntranetEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private IntranetEntity buildDefault(String username) {
        Set<DailyTimeReport> timesTracking = new HashSet<>();
        List<TimeByDay> timeByDays = new ArrayList<>();

        TimeByDay timeByDay = new TimeByDay(LocalDate.of(2014, 2, 17), LocalDate.of(2014, 12, 31),
                Duration.ofHours(8).plusMinutes(41));
        timeByDays.add(timeByDay);

        timeByDay = new TimeByDay(LocalDate.of(2015, 1, 5), LocalDate.of(2015, 12, 30),
                Duration.ofHours(8).plusMinutes(38));
        timeByDays.add(timeByDay);

        timeByDay = new TimeByDay(LocalDate.of(2016, 1, 4), LocalDate.of(2016, 12, 30),
                Duration.ofHours(8).plusMinutes(32));
        timeByDays.add(timeByDay);

        Duration initialTime = Duration.ZERO;
        LocalDate now = LocalDate.now();
        LocalDate dayAtInitialTime;

        if (now.getDayOfMonth() > 20) {
            dayAtInitialTime = LocalDate.of(now.getYear(), now.getMonth(), 15);
        } else {
            LocalDate ltMonth = now.minusMonths(1);
            dayAtInitialTime = LocalDate.of(ltMonth.getYear(), ltMonth.getMonth(), 15);
        }

        Duration actualBalance = Duration.ZERO;

        return new IntranetEntity(username, timesTracking, timeByDays, initialTime, dayAtInitialTime);
    }

    private Path getPath(String username) {
        return Paths.get(username + FILE_SUFIX);
    }

    private Path getTmpPath(String username) {
        return Paths.get(username + FILE_SUFIX_TMP);
    }
}
