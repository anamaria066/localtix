package ro.ase.localtix.service;

/**
 * Thrown when a ticket cannot be cancelled anymore, e.g. the event has already started (US-10).
 */
public class RefundNotAllowedException extends IllegalArgumentException {

    public RefundNotAllowedException(String message) {
        super(message);
    }
}
