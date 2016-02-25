package ponto.intranet.exception;

/**
 * @author gabriel.schmoeller
 */
public class ErrorException extends RuntimeException {

    public ErrorException() {
    }

    public ErrorException(String message) {
        super(message);
    }

    public ErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorException(Throwable cause) {
        super(cause);
    }

    public ErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
