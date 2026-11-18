package ro.ase.localtix.service;

/**
 * Thrown when a customer tries to reserve a seat that is sold
 * or locked in another customer's cart (US-7).
 */
public class SeatUnavailableException extends IllegalArgumentException {

    public SeatUnavailableException(String message) {
        super(message);
    }
}
