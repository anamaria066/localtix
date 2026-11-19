package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RefundServiceTest {

    private static final BigDecimal PAID = new BigDecimal("150.00");
    private static final String EVENT_STARTED = "This ticket can no longer be cancelled: the event has already started";

    private final LocalDateTime eventStart = LocalDateTime.of(2026, 12, 15, 20, 0);

    private RefundService service;
    private Seat seat;

    @BeforeEach
    void setUp() {
        service = new RefundService();
        Event event = new Event("Metrophonic Live", EventCategory.CONCERT, "București", "Sala Palatului",
                eventStart, PAID, "Rock concert");
        seat = new Seat(event, "A", 1, SeatStatus.SOLD);
    }

    // --- AC1: if the organizer cancels the event, the customer gets 100% ---

    // AC1: organizer cancels long before the event -> full refund
    @Test
    void organizerCancellationRefundsFullPrice() {
        BigDecimal refund = service.cancel(seat, PAID, true, eventStart.minusDays(10));

        assertEquals(new BigDecimal("150.00"), refund);
    }

    // AC1: organizer cancels within the last 48 hours -> still full refund (no fee)
    @Test
    void organizerCancellationRefundsFullPriceEvenWithinLast48Hours() {
        BigDecimal refund = service.cancel(seat, PAID, true, eventStart.minusHours(2));

        assertEquals(new BigDecimal("150.00"), refund);
    }

    // --- AC2: customer cancels more than 48h before -> 100% minus a 10% processing fee ---

    // AC2: 3 days before -> 90% of the price
    @Test
    void customerCancellingMoreThan48HoursBeforeGets90Percent() {
        BigDecimal refund = service.cancel(seat, PAID, false, eventStart.minusDays(3));

        assertEquals(new BigDecimal("135.00"), refund);
    }

    // AC2: 48 hours and 1 minute before is still "more than 48h"
    @Test
    void customerCancellingJustOver48HoursBeforeGets90Percent() {
        BigDecimal refund = service.cancel(seat, PAID, false, eventStart.minusHours(48).minusMinutes(1));

        assertEquals(new BigDecimal("135.00"), refund);
    }

    // AC2: the refund is rounded to 2 decimals (99.99 * 90% = 89.991 -> 89.99)
    @Test
    void refundIsRoundedToTwoDecimals() {
        BigDecimal refund = service.cancel(seat, new BigDecimal("99.99"), false, eventStart.minusDays(5));

        assertEquals(new BigDecimal("89.99"), refund);
    }

    // --- AC3: customer cancels less than 48h before -> nothing ---

    // AC3: 47 hours before -> 0
    @Test
    void customerCancellingLessThan48HoursBeforeGetsNothing() {
        BigDecimal refund = service.cancel(seat, PAID, false, eventStart.minusHours(47));

        assertEquals(new BigDecimal("0.00"), refund);
    }

    // AC3: exactly 48 hours before is not "more than 48h" -> 0
    @Test
    void customerCancellingExactly48HoursBeforeGetsNothing() {
        BigDecimal refund = service.cancel(seat, PAID, false, eventStart.minusHours(48));

        assertEquals(new BigDecimal("0.00"), refund);
    }

    // --- AC4: after the event has started, the ticket cannot be cancelled ---

    // AC4: one hour after the start -> error
    @Test
    void ticketCannotBeCancelledAfterEventStarted() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.cancel(seat, PAID, false, eventStart.plusHours(1)));

        assertEquals(EVENT_STARTED, ex.getMessage());
    }

    // AC4: exactly at the start time -> error
    @Test
    void ticketCannotBeCancelledWhenEventStartsNow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cancel(seat, PAID, false, eventStart));
    }

    // AC4: a refused cancellation does not free the seat
    @Test
    void refusedCancellationKeepsSeatSold() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cancel(seat, PAID, false, eventStart.plusHours(1)));

        assertEquals(SeatStatus.SOLD, seat.getStatus());
    }

    // --- AC5: after cancellation the seat is available again and the customer sees the refund ---

    // AC5: the seat becomes available again
    @Test
    void cancelledSeatBecomesAvailable() {
        service.cancel(seat, PAID, false, eventStart.minusDays(3));

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getLockedUntil());
        assertNull(seat.getLockedByUserId());
    }

    // AC5: the seat is freed even when the customer gets nothing back
    @Test
    void seatIsFreedEvenWithoutRefund() {
        service.cancel(seat, PAID, false, eventStart.minusHours(10));

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
    }
}
