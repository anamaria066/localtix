package ro.ase.localtix.service;

/**
 * Thrown when the customer tries to cancel a ticket that is already cancelled (US-12).
 */
public class TicketAlreadyCancelledException extends IllegalArgumentException {

    public TicketAlreadyCancelledException(String message) {
        super(message);
    }
}
