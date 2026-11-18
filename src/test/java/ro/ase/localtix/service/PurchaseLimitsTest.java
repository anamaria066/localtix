package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseLimitsTest {

    private static final String TOO_MANY_TICKETS = "You can buy at most 6 tickets per order";
    private static final String SOLD_OUT = "This event is sold out";
    private static final String AGE_RESTRICTED = "You must be at least 18 years old to buy tickets for this event";

    private final LocalDate today = LocalDate.of(2026, 11, 18);

    private PurchaseLimitsValidator validator;
    private Event adultsOnlyEvent;
    private Event eventForEveryone;

    @BeforeEach
    void setUp() {
        validator = new PurchaseLimitsValidator();

        adultsOnlyEvent = new Event("Late Night Roast", EventCategory.STANDUP, "București", "Club Control",
                LocalDateTime.of(2026, 12, 10, 22, 0), BigDecimal.valueOf(70), "Uncensored stand-up");
        adultsOnlyEvent.setAgeRestriction(18);

        eventForEveryone = new Event("Hamlet", EventCategory.THEATRE, "București", "Teatrul Național",
                LocalDateTime.of(2026, 11, 25, 18, 0), BigDecimal.valueOf(100), "Shakespeare");
    }

    // --- AC1: maximum 6 tickets per order ---

    // AC1: one ticket is allowed
    @Test
    void oneTicketIsAllowed() {
        assertDoesNotThrow(() -> validator.validateTicketCount(1));
    }

    // AC1: exactly 6 tickets is still allowed (the limit itself)
    @Test
    void sixTicketsAreAllowed() {
        assertDoesNotThrow(() -> validator.validateTicketCount(6));
    }

    // AC1 + AC4: 7 tickets are rejected with a message explaining the limit
    @Test
    void sevenTicketsAreRejectedWithAClearMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validateTicketCount(7));

        assertEquals(TOO_MANY_TICKETS, ex.getMessage());
    }

    // --- AC2: a sold-out event cannot be bought; the buy button becomes inactive ---

    // AC2: an event with no available seats is sold out (button inactive)
    @Test
    void eventWithNoAvailableSeatsIsSoldOut() {
        assertTrue(validator.isSoldOut(0));
    }

    // AC2: an event with available seats is not sold out (button active)
    @Test
    void eventWithAvailableSeatsIsNotSoldOut() {
        assertFalse(validator.isSoldOut(5));
    }

    // AC2 + AC4: buying for a sold-out event is rejected with a message explaining why
    @Test
    void buyingForSoldOutEventIsRejectedWithAClearMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validateNotSoldOut(0));

        assertEquals(SOLD_OUT, ex.getMessage());
    }

    // AC2: buying for an event with available seats is allowed
    @Test
    void buyingForEventWithAvailableSeatsIsAllowed() {
        assertDoesNotThrow(() -> validator.validateNotSoldOut(3));
    }

    // --- AC3: only adults can buy tickets for 18+ events (based on the birth date) ---

    // AC3: an adult (25 years old) can buy for an 18+ event
    @Test
    void adultCanBuyForAdultsOnlyEvent() {
        LocalDate birthDate = today.minusYears(25);

        assertDoesNotThrow(() -> validator.validateAge(adultsOnlyEvent, birthDate, today));
    }

    // AC3: a customer who turns 18 exactly today can buy for an 18+ event
    @Test
    void customerTurning18TodayCanBuyForAdultsOnlyEvent() {
        LocalDate birthDate = today.minusYears(18);

        assertDoesNotThrow(() -> validator.validateAge(adultsOnlyEvent, birthDate, today));
    }

    // AC3 + AC4: a minor (16 years old) is rejected with a message explaining why
    @Test
    void minorIsRejectedForAdultsOnlyEventWithAClearMessage() {
        LocalDate birthDate = today.minusYears(16);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validateAge(adultsOnlyEvent, birthDate, today));

        assertEquals(AGE_RESTRICTED, ex.getMessage());
    }

    // AC3: a customer who turns 18 tomorrow is still a minor today
    @Test
    void customerTurning18TomorrowIsRejectedForAdultsOnlyEvent() {
        LocalDate birthDate = today.minusYears(18).plusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateAge(adultsOnlyEvent, birthDate, today));
    }

    // AC3: a minor can buy for an event without age restriction
    @Test
    void minorCanBuyForEventWithoutAgeRestriction() {
        LocalDate birthDate = today.minusYears(16);

        assertDoesNotThrow(() -> validator.validateAge(eventForEveryone, birthDate, today));
    }
}
