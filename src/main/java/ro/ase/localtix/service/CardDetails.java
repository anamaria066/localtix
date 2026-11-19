package ro.ase.localtix.service;

/**
 * The card data typed by the customer on the payment page (US-11).
 * The payment is simulated: the card is only validated, never sent anywhere.
 */
public record CardDetails(String cardNumber, int expiryMonth, int expiryYear, String cvv) {
}
