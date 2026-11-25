package exception;

/**
 * Base checked exception for all domain-specific errors in SmartPark Pro.
 */
public class ParkingException extends Exception {

    public ParkingException(String message) {
        super(message);
    }

    public ParkingException(String message, Throwable cause) {
        super(message, cause);
    }
}
