package exception;

/**
 * Thrown when no compatible free parking slot is available for a vehicle.
 */
public class NoAvailableSlotException extends ParkingException {

    public NoAvailableSlotException(String message) {
        super(message);
    }
}
