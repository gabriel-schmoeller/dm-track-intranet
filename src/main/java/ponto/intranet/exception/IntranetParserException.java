package ponto.intranet.exception;

/**
 * @author gabriel.schmoeller
 */
public class IntranetParserException extends ErrorException {

    public IntranetParserException(String message) {
        super(message);
    }

    public IntranetParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
