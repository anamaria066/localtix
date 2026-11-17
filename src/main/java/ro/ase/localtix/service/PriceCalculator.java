package ro.ase.localtix.service;

import ro.ase.localtix.model.TicketCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Ticket price logic (US-6).
 * Discounts do not add up - only the biggest one is applied.
 */
public class PriceCalculator {

    /** Early bird applies if the ticket is bought MORE than this many days before the event. */
    public static final int EARLY_BIRD_MIN_DAYS = 14;

    /** Early bird discount, in percent. */
    public static final int EARLY_BIRD_DISCOUNT_PERCENT = 20;

    public static final String NEGATIVE_BASE_PRICE = "Base price cannot be negative";

    private static final int PRICE_DECIMALS = 2;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    /**
     * Calculates the final ticket price.
     *
     * @param basePrice  the event's base price
     * @param category   the customer's price category
     * @param daysBefore how many days before the event the ticket is bought
     * @return the final price, rounded to 2 decimals
     * @throws IllegalArgumentException if the base price is negative
     */
    public BigDecimal calculate(BigDecimal basePrice, TicketCategory category, long daysBefore) {
        validateBasePrice(basePrice);
        int discountPercent = getDiscountPercent(category, daysBefore);
        return applyDiscount(basePrice, discountPercent);
    }

    /**
     * Returns the discount applied (in percent) - only the biggest one.
     */
    public int getDiscountPercent(TicketCategory category, long daysBefore) {
        return Math.max(category.getDiscountPercent(), earlyBirdDiscountPercent(daysBefore));
    }

    private void validateBasePrice(BigDecimal basePrice) {
        if (basePrice.signum() < 0) {
            throw new IllegalArgumentException(NEGATIVE_BASE_PRICE);
        }
    }

    private int earlyBirdDiscountPercent(long daysBefore) {
        return isEarlyBird(daysBefore) ? EARLY_BIRD_DISCOUNT_PERCENT : 0;
    }

    private boolean isEarlyBird(long daysBefore) {
        return daysBefore > EARLY_BIRD_MIN_DAYS;
    }

    /** E.g. 30% off -> price * 70 / 100, rounded to 2 decimals. */
    private BigDecimal applyDiscount(BigDecimal basePrice, int discountPercent) {
        BigDecimal remainingPercent = BigDecimal.valueOf(100 - discountPercent);
        return basePrice.multiply(remainingPercent)
                .divide(ONE_HUNDRED, PRICE_DECIMALS, RoundingMode.HALF_UP);
    }
}
