package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeatReservationServiceTest {

    private static final Long ANA = 1L;
    private static final Long MARIA = 2L;
    private static final String SEAT_SOLD = "This seat is already sold";
    private static final String SEAT_LOCKED = "This seat has just been reserved by another customer";

    private final LocalDateTime now = LocalDateTime.of(2026, 11, 18, 12, 0);

    private SeatReservationService service;
    private Event event;

    @BeforeEach
    void setUp() {
        service = new SeatReservationService();
        event = new Event("Metrophonic Live", EventCategory.CONCERT, "București", "Sala Palatului",
                now.plusDays(30), BigDecimal.valueOf(150), "Rock concert");
    }

    private Seat seat(SeatStatus status) {
        return new Seat(event, "A", 1, status);
    }

    // --- AC1: the map shows available, sold and temporarily locked seats differently ---

    // AC1: an available seat is shown as available
    @Test
    void availableSeatIsShownAsAvailable() {
        assertEquals(SeatStatus.AVAILABLE, service.getDisplayStatus(seat(SeatStatus.AVAILABLE), now));
    }

    // AC1: a sold seat is shown as sold
    @Test
    void soldSeatIsShownAsSold() {
        assertEquals(SeatStatus.SOLD, service.getDisplayStatus(seat(SeatStatus.SOLD), now));
    }

    // AC1: a seat locked in someone's cart is shown as locked
    @Test
    void lockedSeatIsShownAsLocked() {
        Seat seat = seat(SeatStatus.AVAILABLE);
        service.reserve(seat, ANA, now);

        assertEquals(SeatStatus.LOCKED, service.getDisplayStatus(seat, now.plusMinutes(5)));
    }

    // AC1 + AC4: a seat whose lock has expired is shown as available again
    @Test
    void seatWithExpiredLockIsShownAsAvailable() {
        Seat seat = seat(SeatStatus.AVAILABLE);
        service.reserve(seat, ANA, now);

        assertEquals(SeatStatus.AVAILABLE, service.getDisplayStatus(seat, now.plusMinutes(10)));
    }

    // --- AC2: a sold seat cannot be selected ---

    // AC2: reserving a sold seat throws an error with a clear message
    @Test
    void soldSeatCannotBeReserved() {
        Seat seat = seat(SeatStatus.SOLD);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.reserve(seat, ANA, now));

        assertEquals(SEAT_SOLD, ex.getMessage());
        assertEquals(SeatStatus.SOLD, seat.getStatus());
    }

    // --- AC3: a seat put in the cart is locked for 10 minutes ---

    // AC3: after reserving, the seat is LOCKED for the customer who chose it
    @Test
    void reservedSeatIsLockedForTheCustomer() {
        Seat seat = seat(SeatStatus.AVAILABLE);

        service.reserve(seat, ANA, now);

        assertEquals(SeatStatus.LOCKED, seat.getStatus());
        assertEquals(ANA, seat.getLockedByUserId());
    }

    // AC3: the lock lasts exactly 10 minutes
    @Test
    void lockLastsTenMinutes() {
        Seat seat = seat(SeatStatus.AVAILABLE);

        service.reserve(seat, ANA, now);

        assertEquals(now.plusMinutes(10), seat.getLockedUntil());
    }

    // AC3: during the 10 minutes, another customer cannot take the seat
    @Test
    void lockedSeatCannotBeReservedByAnotherCustomerWithinTenMinutes() {
        Seat seat = seat(SeatStatus.AVAILABLE);
        service.reserve(seat, ANA, now);

        assertThrows(IllegalArgumentException.class,
                () -> service.reserve(seat, MARIA, now.plusMinutes(9)));
        assertEquals(ANA, seat.getLockedByUserId());
    }

    // --- AC4: if the payment is not made in 10 minutes, the seat is released automatically ---

    // AC4: after 10 minutes, the expired lock is released
    @Test
    void expiredLockIsReleased() {
        Seat seat = seat(SeatStatus.LOCKED);
        seat.setLockedByUserId(ANA);
        seat.setLockedUntil(now.plusMinutes(10));

        service.releaseExpiredLocks(List.of(seat), now.plusMinutes(10));

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getLockedUntil());
        assertNull(seat.getLockedByUserId());
    }

    // AC4: a lock that has not expired yet stays in place
    @Test
    void activeLockIsNotReleased() {
        Seat seat = seat(SeatStatus.AVAILABLE);
        service.reserve(seat, ANA, now);

        service.releaseExpiredLocks(List.of(seat), now.plusMinutes(9));

        assertEquals(SeatStatus.LOCKED, seat.getStatus());
        assertEquals(ANA, seat.getLockedByUserId());
    }

    // AC4: releasing expired locks never touches sold seats
    @Test
    void soldSeatsAreNotReleased() {
        Seat seat = seat(SeatStatus.SOLD);

        service.releaseExpiredLocks(List.of(seat), now);

        assertEquals(SeatStatus.SOLD, seat.getStatus());
    }

    // AC4: once the lock has expired, another customer can take the seat
    @Test
    void anotherCustomerCanReserveAfterLockExpires() {
        Seat seat = seat(SeatStatus.AVAILABLE);
        service.reserve(seat, ANA, now);

        service.reserve(seat, MARIA, now.plusMinutes(11));

        assertEquals(SeatStatus.LOCKED, seat.getStatus());
        assertEquals(MARIA, seat.getLockedByUserId());
        assertEquals(now.plusMinutes(21), seat.getLockedUntil());
    }

    // --- AC5: if two customers choose the same seat, only the first one gets it ---

    // AC5: the first customer gets the seat, the second one sees an error
    @Test
    void secondCustomerChoosingTheSameSeatGetsAnError() {
        Seat seat = seat(SeatStatus.AVAILABLE);

        service.reserve(seat, ANA, now);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.reserve(seat, MARIA, now));

        assertEquals(SEAT_LOCKED, ex.getMessage());
        assertEquals(ANA, seat.getLockedByUserId());
    }
}
