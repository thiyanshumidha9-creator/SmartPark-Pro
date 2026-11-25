package exception;

/**
 * Thrown when an entry/exit timestamp or parking duration is invalid.
 */
public class InvalidTimeException extends ParkingException {

    public InvalidTimeException(String message) {
        super(message);
    }

    public InvalidTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
