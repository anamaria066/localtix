package ro.ase.localtix.model;

/** Status of an electronic ticket (US-12): active after payment, cancelled after a refund. */
public enum TicketStatus {
    ACTIVE("Active"),
    CANCELLED("Cancelled");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    /** The label shown to the customer in "My Tickets". */
    public String getDisplayName() {
        return displayName;
    }
}
