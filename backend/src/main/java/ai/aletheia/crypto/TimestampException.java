package ai.aletheia.crypto;

/**
 * Thrown when timestamping fails: TSA unreachable, invalid response, or parse error.
 */
public class TimestampException extends RuntimeException {

    public TimestampException(String message) {
        super(message);
    }

    public TimestampException(String message, Throwable cause) {
        super(message, cause);
    }
}
