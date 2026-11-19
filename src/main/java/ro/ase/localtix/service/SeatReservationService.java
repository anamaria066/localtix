package ro.ase.localtix.service;

import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seat selection logic (US-7).
 * Plain Java: receives the seats, the customer's id and the current time as parameters.
 */
public class SeatReservationService {

    /** How long a seat put in the cart stays locked for the other customers. */
    public static final int LOCK_DURATION_MINUTES = 10;

    public static final String SEAT_SOLD = "This seat is already sold";
    public static final String SEAT_LOCKED = "This seat has just been reserved by another customer";

    /**
     * Puts the seat in the customer's cart and locks it for the other customers.
     *
     * @param seat   the chosen seat
     * @param userId the customer who chose it
     * @param now    the current time
     * @throws SeatUnavailableException if the seat is sold or locked by someone else
     */
    public void reserve(Seat seat, Long userId, LocalDateTime now) {
        if (isSold(seat)) {
            throw new SeatUnavailableException(SEAT_SOLD);
        }
        if (hasActiveLock(seat, now)) {
            throw new SeatUnavailableException(SEAT_LOCKED);
        }
        lock(seat, userId, now);
    }

    /** Frees the seats whose lock has expired (cart not paid in time). */
    public void releaseExpiredLocks(List<Seat> seats, LocalDateTime now) {
        seats.stream()
                .filter(seat -> hasExpiredLock(seat, now))
                .forEach(this::unlock);
    }

    /** Frees one seat, e.g. when the customer removes it from the cart (US-9). */
    public void release(Seat seat) {
        unlock(seat);
    }

    /** The status shown on the seat map at the moment "now": an expired lock counts as available. */
    public SeatStatus getDisplayStatus(Seat seat, LocalDateTime now) {
        return hasExpiredLock(seat, now) ? SeatStatus.AVAILABLE : seat.getStatus();
    }

    private boolean isSold(Seat seat) {
        return seat.getStatus() == SeatStatus.SOLD;
    }

    private boolean isLocked(Seat seat) {
        return seat.getStatus() == SeatStatus.LOCKED;
    }

    /** Locked and the 10 minutes have not passed yet. */
    private boolean hasActiveLock(Seat seat, LocalDateTime now) {
        return isLocked(seat) && now.isBefore(seat.getLockedUntil());
    }

    /** Locked, but the 10 minutes have passed. */
    private boolean hasExpiredLock(Seat seat, LocalDateTime now) {
        return isLocked(seat) && !now.isBefore(seat.getLockedUntil());
    }

    private void lock(Seat seat, Long userId, LocalDateTime now) {
        seat.setStatus(SeatStatus.LOCKED);
        seat.setLockedByUserId(userId);
        seat.setLockedUntil(now.plusMinutes(LOCK_DURATION_MINUTES));
    }

    private void unlock(Seat seat) {
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setLockedByUserId(null);
        seat.setLockedUntil(null);
    }
}
