package ro.ase.localtix.service;

/**
 * Thrown when a purchase breaks one of the purchase limits (US-8):
 * too many tickets, sold-out event or customer too young for the event.
 */
public class PurchaseLimitException extends IllegalArgumentException {

    public PurchaseLimitException(String message) {
        super(message);
    }
}
