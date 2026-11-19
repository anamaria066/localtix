package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;
import ro.ase.localtix.model.Order;
import ro.ase.localtix.model.OrderStatus;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private static final Long DAVID = 1L;
    private static final String VALID_CARD = "4111111111111111";
    private static final String VALID_CVV = "123";

    private static final String INVALID_CARD_NUMBER = "Card number must have 16 digits and be valid";
    private static final String CARD_EXPIRED = "The card has expired";
    private static final String INVALID_CVV = "CVV must have 3 digits";
    private static final String RESERVATION_EXPIRED =
            "Your reservation has expired. Please choose your seats again.";

    /** 18 November 2026, 12:00 */
    private final LocalDateTime now = LocalDateTime.of(2026, 11, 18, 12, 0);

    private PaymentService service;
    private Seat seatA1;
    private Seat seatA2;
    private Order order;

    @BeforeEach
    void setUp() {
        service = new PaymentService();
        Event event = new Event("Hamlet", EventCategory.THEATRE, "București", "Teatrul Național",
                now.plusDays(7), new BigDecimal("100.00"), "Shakespeare");
        seatA1 = lockedSeat(event, 1, now.plusMinutes(5));
        seatA2 = lockedSeat(event, 2, now.plusMinutes(5));
        order = new Order(DAVID, List.of(seatA1, seatA2), new BigDecimal("200.00"), now);
    }

    private Seat lockedSeat(Event event, int number, LocalDateTime lockedUntil) {
        Seat seat = new Seat(event, "A", number, SeatStatus.LOCKED);
        seat.setLockedByUserId(DAVID);
        seat.setLockedUntil(lockedUntil);
        return seat;
    }

    private CardDetails card(String number, int month, int year, String cvv) {
        return new CardDetails(number, month, year, cvv);
    }

    private CardDetails validCard() {
        return card(VALID_CARD, 12, 2028, VALID_CVV);
    }

    private void assertPaymentFails(CardDetails card, String expectedMessage) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.pay(order, card, now));
        assertEquals(expectedMessage, e.getMessage());
    }

    // --- AC2: the card number has 16 digits and is valid according to the Luhn algorithm ---

    // AC1 + AC2: a valid card is accepted by the simulated payment
    @Test
    void paymentWithValidCardSucceeds() {
        service.pay(order, validCard(), now);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    // AC2: another card number valid with Luhn is accepted
    @Test
    void anotherLuhnValidCardIsAccepted() {
        service.pay(order, card("5555555555554444", 12, 2028, VALID_CVV), now);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    // AC2: the card number may be typed in groups of 4 digits
    @Test
    void cardNumberWithSpacesIsAccepted() {
        service.pay(order, card("4111 1111 1111 1111", 12, 2028, VALID_CVV), now);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    // AC2: fewer than 16 digits
    @Test
    void cardNumberWith15DigitsIsRejected() {
        assertPaymentFails(card("411111111111111", 12, 2028, VALID_CVV), INVALID_CARD_NUMBER);
    }

    // AC2: more than 16 digits
    @Test
    void cardNumberWith17DigitsIsRejected() {
        assertPaymentFails(card("41111111111111111", 12, 2028, VALID_CVV), INVALID_CARD_NUMBER);
    }

    // AC2: letters are not allowed
    @Test
    void cardNumberWithLettersIsRejected() {
        assertPaymentFails(card("4111abcd11111111", 12, 2028, VALID_CVV), INVALID_CARD_NUMBER);
    }

    // AC2: 16 digits, but the Luhn check fails
    @Test
    void cardNumberFailingLuhnCheckIsRejected() {
        assertPaymentFails(card("4111111111111112", 12, 2028, VALID_CVV), INVALID_CARD_NUMBER);
    }

    // --- AC3: the expiry date is not in the past; the CVV has 3 digits ---

    // AC3: card expired last month
    @Test
    void cardExpiredLastMonthIsRejected() {
        assertPaymentFails(card(VALID_CARD, 10, 2026, VALID_CVV), CARD_EXPIRED);
    }

    // AC3: card expired last year (later month, earlier year)
    @Test
    void cardExpiredLastYearIsRejected() {
        assertPaymentFails(card(VALID_CARD, 12, 2025, VALID_CVV), CARD_EXPIRED);
    }

    // AC3: a card is valid until the end of its expiry month
    @Test
    void cardExpiringThisMonthIsAccepted() {
        service.pay(order, card(VALID_CARD, 11, 2026, VALID_CVV), now);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    // AC3: CVV with 2 digits
    @Test
    void cvvWithTwoDigitsIsRejected() {
        assertPaymentFails(card(VALID_CARD, 12, 2028, "12"), INVALID_CVV);
    }

    // AC3: CVV with 4 digits
    @Test
    void cvvWithFourDigitsIsRejected() {
        assertPaymentFails(card(VALID_CARD, 12, 2028, "1234"), INVALID_CVV);
    }

    // AC3: CVV with letters
    @Test
    void cvvWithLettersIsRejected() {
        assertPaymentFails(card(VALID_CARD, 12, 2028, "12a"), INVALID_CVV);
    }

    // --- AC4: on successful payment the order becomes "Paid" and the seats become "Sold" ---

    // AC4: all the seats of the order become SOLD
    @Test
    void successfulPaymentMarksSeatsAsSold() {
        service.pay(order, validCard(), now);
        assertEquals(SeatStatus.SOLD, seatA1.getStatus());
        assertEquals(SeatStatus.SOLD, seatA2.getStatus());
    }

    // AC4: a sold seat is no longer locked in anyone's cart
    @Test
    void successfulPaymentClearsTheSeatLocks() {
        service.pay(order, validCard(), now);
        assertNull(seatA1.getLockedUntil());
        assertNull(seatA1.getLockedByUserId());
    }

    // --- AC5: on failed payment an error is shown and the seats stay locked until the 10 minutes expire ---

    // AC5: failed payment -> the seats stay LOCKED for the same customer, until the same time
    @Test
    void failedPaymentKeepsSeatsLocked() {
        assertPaymentFails(card(VALID_CARD, 12, 2028, "12"), INVALID_CVV);
        assertEquals(SeatStatus.LOCKED, seatA1.getStatus());
        assertEquals(DAVID, seatA1.getLockedByUserId());
        assertEquals(now.plusMinutes(5), seatA1.getLockedUntil());
    }

    // AC5: failed payment -> the order is not paid
    @Test
    void failedPaymentKeepsOrderPending() {
        assertPaymentFails(card("4111111111111112", 12, 2028, VALID_CVV), INVALID_CARD_NUMBER);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    // AC5: after the 10 minutes the seats are released, so the order can no longer be paid
    @Test
    void paymentAfterReservationExpiredIsRejected() {
        seatA2.setLockedUntil(now.minusMinutes(1));
        assertPaymentFails(validCard(), RESERVATION_EXPIRED);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(SeatStatus.LOCKED, seatA1.getStatus());
    }
}
