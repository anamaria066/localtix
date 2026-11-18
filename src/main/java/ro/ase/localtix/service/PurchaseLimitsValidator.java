package ro.ase.localtix.service;

import ro.ase.localtix.model.Event;

import java.time.LocalDate;
import java.time.Period;

/**
 * Purchase limits (US-8): max tickets per order, sold-out events, age-restricted events.
 * Plain Java: the current date is received as a parameter.
 */
public class PurchaseLimitsValidator {

    /** AC1: maximum number of tickets in one order. */
    public static final int MAX_TICKETS_PER_ORDER = 6;

    // AC4: messages explaining why the purchase is not allowed
    public static final String TOO_MANY_TICKETS =
            "You can buy at most " + MAX_TICKETS_PER_ORDER + " tickets per order";
    public static final String SOLD_OUT = "This event is sold out";
    /** %d = the event's minimum age. */
    public static final String AGE_RESTRICTED = "You must be at least %d years old to buy tickets for this event";

    /**
     * AC1: checks the number of tickets in the order.
     *
     * @throws PurchaseLimitException if there are more than {@value #MAX_TICKETS_PER_ORDER} tickets
     */
    public void validateTicketCount(int ticketCount) {
        if (ticketCount > MAX_TICKETS_PER_ORDER) {
            throw new PurchaseLimitException(TOO_MANY_TICKETS);
        }
    }

    /** AC2: true if the event has no available seats left (the buy button becomes inactive). */
    public boolean isSoldOut(long availableSeats) {
        return availableSeats <= 0;
    }

    /**
     * AC2: checks that the event still has available seats.
     *
     * @throws PurchaseLimitException if the event is sold out
     */
    public void validateNotSoldOut(long availableSeats) {
        if (isSoldOut(availableSeats)) {
            throw new PurchaseLimitException(SOLD_OUT);
        }
    }

    /**
     * AC3: checks that the customer is old enough for the event (e.g. 18+ events).
     *
     * @param birthDate the customer's birth date (from the account)
     * @param today     the current date
     * @throws PurchaseLimitException if the customer is younger than the event's age restriction
     */
    public void validateAge(Event event, LocalDate birthDate, LocalDate today) {
        int minimumAge = event.getAgeRestriction();
        if (!isOldEnough(birthDate, today, minimumAge)) {
            throw new PurchaseLimitException(AGE_RESTRICTED.formatted(minimumAge));
        }
    }

    private boolean isOldEnough(LocalDate birthDate, LocalDate today, int minimumAge) {
        return calculateAge(birthDate, today) >= minimumAge;
    }

    /** Age in completed years: someone who turns 18 tomorrow is still 17 today. */
    private int calculateAge(LocalDate birthDate, LocalDate today) {
        return Period.between(birthDate, today).getYears();
    }
}
