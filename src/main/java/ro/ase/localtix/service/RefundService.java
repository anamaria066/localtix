package ro.ase.localtix.service;

import ro.ase.localtix.model.Seat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Ticket cancellation and refund logic (US-10).
 * Plain Java: receives the seat, the price paid and the current time as parameters.
 */
public class RefundService {

    /** The customer gets money back only if they cancel MORE than this many hours before the event. */
    public static final int REFUND_MIN_HOURS_BEFORE = 48;

    /** Fee kept when the customer cancels in time, in percent. */
    public static final int PROCESSING_FEE_PERCENT = 10;

    public static final String EVENT_ALREADY_STARTED =
            "This ticket can no longer be cancelled: the event has already started";

    private static final int MONEY_DECIMALS = 2;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final SeatReservationService seatReservationService = new SeatReservationService();

    /**
     * Cancels the ticket for this seat and returns the amount refunded to the customer.
     *
     * @param seat                 the seat of the cancelled ticket
     * @param paidPrice            the price the customer paid for the ticket
     * @param cancelledByOrganizer true if the organizer cancelled the whole event
     * @param now                  the current time
     * @return the refunded amount, rounded to 2 decimals
     * @throws RefundNotAllowedException if the event has already started
     */
    public BigDecimal cancel(Seat seat, BigDecimal paidPrice, boolean cancelledByOrganizer, LocalDateTime now) {
        LocalDateTime eventStart = seat.getEvent().getDateTime();
        validateNotStarted(eventStart, now);

        BigDecimal refund = calculateRefund(paidPrice, eventStart, cancelledByOrganizer, now);
        seatReservationService.release(seat);
        return refund;
    }

    private void validateNotStarted(LocalDateTime eventStart, LocalDateTime now) {
        if (!now.isBefore(eventStart)) {
            throw new RefundNotAllowedException(EVENT_ALREADY_STARTED);
        }
    }

    /** Organizer cancelled -> 100%; customer in time -> 100% minus the fee; customer late -> nothing. */
    private BigDecimal calculateRefund(BigDecimal paidPrice, LocalDateTime eventStart,
                                       boolean cancelledByOrganizer, LocalDateTime now) {
        if (cancelledByOrganizer) {
            return toMoney(paidPrice);
        }
        if (isCancelledInTime(eventStart, now)) {
            return deductProcessingFee(paidPrice);
        }
        return toMoney(BigDecimal.ZERO);
    }

    /** True if there are MORE than 48 hours left until the event. */
    private boolean isCancelledInTime(LocalDateTime eventStart, LocalDateTime now) {
        return now.isBefore(eventStart.minusHours(REFUND_MIN_HOURS_BEFORE));
    }

    /** E.g. 10% fee -> price * 90 / 100, rounded to 2 decimals. */
    private BigDecimal deductProcessingFee(BigDecimal paidPrice) {
        BigDecimal refundPercent = BigDecimal.valueOf(100 - PROCESSING_FEE_PERCENT);
        return paidPrice.multiply(refundPercent)
                .divide(ONE_HUNDRED, MONEY_DECIMALS, RoundingMode.HALF_UP);
    }

    private BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(MONEY_DECIMALS, RoundingMode.HALF_UP);
    }
}
