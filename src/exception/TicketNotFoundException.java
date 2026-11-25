package exception;

/**
 * Thrown when a requested active parking ticket or vehicle cannot be found.
 */
public class TicketNotFoundException extends ParkingException {

    public TicketNotFoundException(String message) {
        super(message);
    }
}
