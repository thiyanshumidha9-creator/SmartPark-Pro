package exception;

/**
 * Thrown when saving or loading parking records fails due to an I/O error.
 */
public class DataStorageException extends ParkingException {

    public DataStorageException(String message) {
        super(message);
    }

    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
