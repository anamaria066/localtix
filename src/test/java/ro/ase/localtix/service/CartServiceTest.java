package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;
import ro.ase.localtix.model.TicketCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {

    private static final Long DAVID = 1L;
    private static final Long MARIA = 2L;
    private static final String NOT_IN_CART = "This ticket is not in your cart";
    private static final String TOO_MANY_TICKETS = "You can buy at most 6 tickets per order";
    private static final String SEAT_SOLD = "This seat is already sold";

    private final LocalDateTime now = LocalDateTime.of(2026, 11, 18, 12, 0);

    private CartService service;
    /** 30 days away, 150 RON -> early bird applies. */
    private Event farEvent;
    /** 5 days away, 100 RON -> no early bird. */
    private Event closeEvent;

    @BeforeEach
    void setUp() {
        service = new CartService();
        farEvent = new Event("Metrophonic Live", EventCategory.CONCERT, "București", "Sala Palatului",
                now.plusDays(30), new BigDecimal("150.00"), "Rock concert");
        closeEvent = new Event("Hamlet", EventCategory.THEATRE, "București", "Teatrul Național",
                now.plusDays(5), new BigDecimal("100.00"), "Shakespeare");
    }

    private Seat seat(int number, SeatStatus status) {
        return new Seat(farEvent, "A", number, status);
    }

    private Seat lockedSeat(int number, Long userId, LocalDateTime lockedUntil) {
        Seat seat = seat(number, SeatStatus.LOCKED);
        seat.setLockedByUserId(userId);
        seat.setLockedUntil(lockedUntil);
        return seat;
    }

    // --- AC1: the cart shows the event, the seats, the category, the price per ticket and the total ---

    // AC1: the price of a ticket comes from PriceCalculator (normal + early bird -20%)
    @Test
    void normalTicketPriceIncludesEarlyBirdDiscount() {
        assertEquals(new BigDecimal("120.00"),
                service.calculateTicketPrice(farEvent, TicketCategory.NORMAL, now));
    }

    // AC1: student ticket gets the biggest discount (-30%, not cumulated with early bird)
    @Test
    void studentTicketGetsTheBiggestDiscount() {
        assertEquals(new BigDecimal("105.00"),
                service.calculateTicketPrice(farEvent, TicketCategory.STUDENT, now));
    }

    // AC1: senior ticket close to the event gets -40%
    @Test
    void seniorTicketPriceCloseToEvent() {
        assertEquals(new BigDecimal("60.00"),
                service.calculateTicketPrice(closeEvent, TicketCategory.SENIOR, now));
    }

    // AC1: normal ticket close to the event pays the full price
    @Test
    void normalTicketCloseToEventPaysFullPrice() {
        assertEquals(new BigDecimal("100.00"),
                service.calculateTicketPrice(closeEvent, TicketCategory.NORMAL, now));
    }

    // AC1: the total is the sum of the ticket prices
    @Test
    void totalIsTheSumOfTicketPrices() {
        List<CartItem> items = List.of(
                new CartItem(seat(1, SeatStatus.LOCKED), TicketCategory.NORMAL, new BigDecimal("120.00")),
                new CartItem(seat(2, SeatStatus.LOCKED), TicketCategory.STUDENT, new BigDecimal("105.00")),
                new CartItem(seat(3, SeatStatus.LOCKED), TicketCategory.SENIOR, new BigDecimal("60.00")));

        assertEquals(new BigDecimal("285.00"), service.calculateTotal(items));
    }

    // AC1: an empty cart has the total 0.00
    @Test
    void emptyCartTotalIsZero() {
        assertEquals(new BigDecimal("0.00"), service.calculateTotal(List.of()));
    }

    // AC1: the cart contains only the seats locked by this customer
    @Test
    void cartContainsOnlyTheCustomersLockedSeats() {
        Seat mine = lockedSeat(1, DAVID, now.plusMinutes(5));
        Seat marias = lockedSeat(2, MARIA, now.plusMinutes(5));
        Seat available = seat(3, SeatStatus.AVAILABLE);
        Seat sold = seat(4, SeatStatus.SOLD);

        List<Seat> cart = service.getCartSeats(List.of(mine, marias, available, sold), DAVID, now);

        assertEquals(List.of(mine), cart);
    }

    // AC1 + AC3: a seat whose 10 minutes have passed is no longer in the cart
    @Test
    void expiredSeatIsNotInTheCart() {
        Seat expired = lockedSeat(1, DAVID, now.minusMinutes(1));

        assertTrue(service.getCartSeats(List.of(expired), DAVID, now).isEmpty());
    }

    // --- Adding a ticket to the cart ---

    // Adding a ticket locks the seat for the customer and puts it in the cart
    @Test
    void addedTicketIsInTheCart() {
        Seat seat = seat(1, SeatStatus.AVAILABLE);
        List<Seat> eventSeats = List.of(seat);

        service.addTicket(eventSeats, seat, DAVID, now);

        assertEquals(SeatStatus.LOCKED, seat.getStatus());
        assertEquals(List.of(seat), service.getCartSeats(eventSeats, DAVID, now));
    }

    // A 7th ticket for the same event cannot be added (US-8 limit, reused)
    @Test
    void seventhTicketCannotBeAdded() {
        List<Seat> eventSeats = new ArrayList<>();
        for (int number = 1; number <= 6; number++) {
            eventSeats.add(lockedSeat(number, DAVID, now.plusMinutes(5)));
        }
        Seat seventh = seat(7, SeatStatus.AVAILABLE);
        eventSeats.add(seventh);

        PurchaseLimitException ex = assertThrows(PurchaseLimitException.class,
                () -> service.addTicket(eventSeats, seventh, DAVID, now));

        assertEquals(TOO_MANY_TICKETS, ex.getMessage());
        assertEquals(SeatStatus.AVAILABLE, seventh.getStatus());
    }

    // A sold seat cannot be added to the cart
    @Test
    void soldSeatCannotBeAdded() {
        Seat sold = seat(1, SeatStatus.SOLD);

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
                () -> service.addTicket(List.of(sold), sold, DAVID, now));

        assertEquals(SEAT_SOLD, ex.getMessage());
    }

    // --- AC2: the customer can remove a ticket and the seat is released ---

    // AC2: a removed ticket frees the seat
    @Test
    void removedTicketReleasesTheSeat() {
        Seat seat = lockedSeat(1, DAVID, now.plusMinutes(5));

        service.removeTicket(seat, DAVID, now);

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getLockedByUserId());
        assertNull(seat.getLockedUntil());
    }

    // AC2: a customer cannot remove a seat from someone else's cart
    @Test
    void cannotRemoveAnotherCustomersTicket() {
        Seat marias = lockedSeat(1, MARIA, now.plusMinutes(5));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.removeTicket(marias, DAVID, now));

        assertEquals(NOT_IN_CART, ex.getMessage());
        assertEquals(SeatStatus.LOCKED, marias.getStatus());
        assertEquals(MARIA, marias.getLockedByUserId());
    }

    // --- AC3: the time left from the 10 minutes of reservation is shown ---

    // AC3: right after adding a ticket, 10 minutes (600 seconds) are left
    @Test
    void tenMinutesLeftRightAfterAdding() {
        Seat seat = seat(1, SeatStatus.AVAILABLE);
        service.addTicket(List.of(seat), seat, DAVID, now);

        assertEquals(600, service.getRemainingSeconds(List.of(seat), now));
    }

    // AC3: the time left goes down as time passes
    @Test
    void remainingTimeDecreases() {
        Seat seat = lockedSeat(1, DAVID, now.plusMinutes(10));

        assertEquals(330, service.getRemainingSeconds(List.of(seat), now.plusMinutes(4).plusSeconds(30)));
    }

    // AC3: with several seats, the timer shows the seat that expires first
    @Test
    void remainingTimeIsForTheSeatThatExpiresFirst() {
        Seat first = lockedSeat(1, DAVID, now.plusMinutes(10));
        Seat second = lockedSeat(2, DAVID, now.plusMinutes(13));

        assertEquals(300, service.getRemainingSeconds(List.of(second, first), now.plusMinutes(5)));
    }

    // AC3: when the time is up, the remaining time is 0 (never negative)
    @Test
    void remainingTimeIsZeroWhenExpired() {
        Seat seat = lockedSeat(1, DAVID, now.plusMinutes(10));

        assertEquals(0, service.getRemainingSeconds(List.of(seat), now.plusMinutes(12)));
    }
}
