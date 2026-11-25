package exception;

/**
 * Thrown when vehicle details are invalid, unsupported, or already parked.
 */
public class InvalidVehicleException extends ParkingException {

    public InvalidVehicleException(String message) {
        super(message);
    }
}
