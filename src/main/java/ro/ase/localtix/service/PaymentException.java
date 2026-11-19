package ro.ase.localtix.service;

/**
 * Thrown when a payment fails (US-11): invalid card, expired card, invalid CVV
 * or the reservation of the seats has expired.
 */
public class PaymentException extends IllegalArgumentException {

    public PaymentException(String message) {
        super(message);
    }
}
