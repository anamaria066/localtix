package ro.ase.localtix.model;

/**
 * Price category of the customer buying a ticket (US-6).
 * Not to be confused with EventCategory (concert / stand-up / theatre).
 * Each category knows its own discount, in percent.
 */
public enum TicketCategory {
    NORMAL("Normal", 0),
    STUDENT("Student", 30),
    SENIOR("Senior", 40);

    private final String displayName;
    private final int discountPercent;

    TicketCategory(String displayName, int discountPercent) {
        this.displayName = displayName;
        this.discountPercent = discountPercent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }
}
