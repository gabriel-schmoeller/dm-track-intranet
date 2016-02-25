package ponto.intranet.exception;

/**
 * @author gabriel.schmoeller
 */
public class LoginErrorException extends ErrorException {

    public LoginErrorException(String message) {
        super(message);
    }

    public LoginErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
