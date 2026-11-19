package ro.ase.localtix.model;

/** Status of an order (US-11): created at checkout, paid after a successful card payment. */
public enum OrderStatus {
    PENDING,
    PAID
}
