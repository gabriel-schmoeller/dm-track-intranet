package ponto.intranet.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ponto.intranet.service.domain.DatePeriod;
import ponto.intranet.service.domain.InitialBalance;
import ponto.intranet.service.domain.UserCredentials;
import ponto.intranet.exception.LoginCredentialsException;
import ponto.intranet.exception.ValidationException;
import ponto.intranet.handlers.TimeHandler;

/**
 * @author gabriel.schmoeller
 */
@Component
public class IntranetServiceValidator {

    private final TimeHandler timeHandler;

    @Autowired
    public IntranetServiceValidator(TimeHandler timeHandler) {
        this.timeHandler = timeHandler;
    }

    public void validateLogin(UserCredentials credentials) throws LoginCredentialsException {
        if (isNullOrEmpty(credentials.getUser()) || isNullOrEmpty(credentials.getPass())) {
            throw new LoginCredentialsException("Os campos usuário e senha não podem ser vazios.");
        }
    }

    public void validateInitialBalance(InitialBalance initialBalance) throws ValidationException {
        if (Objects.isNull(initialBalance.getInitialDate())) {
            throw new ValidationException("A data do saldo inicial deve ser preenchido.");
        } else if (isNullOrEmpty(initialBalance.getInitialBalance())) {
            throw new ValidationException("O saldo inicial deve ser preenchido.");
        }

        try {
            timeHandler.parseDuration(initialBalance.getInitialBalance());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("OSaldo inicial deve estar no formato HH:MM ou -HH:MM");
        }
    }

    public void validateDownloadMonths(int months) throws ValidationException {
        if (months < 1) {
            throw new ValidationException("A quantidade de meses a ser baixado deve ser maior do que 0.");
        }
    }

    public void validateCalculateBalance(DatePeriod datePeriod) {
        if (Objects.isNull(datePeriod.getFtDate()) || Objects.isNull(datePeriod.getLtDate())) {
            throw new ValidationException("Ambas as datas 'De' e 'até' devem ser preenchidas.");
        } else if (datePeriod.getFtDate().isAfter(datePeriod.getLtDate())) {
            throw new ValidationException("A data 'De' deve vir antes do que 'até'.");
        }
    }

    private boolean isNullOrEmpty(String string) {
        return Objects.isNull(string) || string.isEmpty();
    }
}
