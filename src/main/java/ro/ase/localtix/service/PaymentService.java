package ro.ase.localtix.service;

import ro.ase.localtix.model.Order;
import ro.ase.localtix.model.OrderStatus;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Simulated online payment (US-11): the card is only validated, no real payment processor is used.
 * Plain Java: receives the order, the card and the current time as parameters.
 */
public class PaymentService {

    public static final int CARD_NUMBER_LENGTH = 16;
    public static final int CVV_LENGTH = 3;

    public static final String INVALID_CARD_NUMBER = "Card number must have 16 digits and be valid";
    public static final String CARD_EXPIRED = "The card has expired";
    public static final String INVALID_CVV = "CVV must have 3 digits";
    public static final String RESERVATION_EXPIRED =
            "Your reservation has expired. Please choose your seats again.";

    private static final String CARD_NUMBER_REGEX = "\\d{" + CARD_NUMBER_LENGTH + "}";
    private static final String CVV_REGEX = "\\d{" + CVV_LENGTH + "}";

    /**
     * Pays the order with the card.
     * On success the order becomes PAID and its seats become SOLD (AC4).
     * On failure nothing changes: the seats stay locked until the 10 minutes expire (AC5).
     *
     * @throws PaymentException with the reason, if the payment fails
     */
    public void pay(Order order, CardDetails card, LocalDateTime now) {
        validateCard(card, now);
        validateReservation(order, now);
        markAsPaid(order);
    }

    /** AC2 + AC3: card number, expiry date and CVV. */
    private void validateCard(CardDetails card, LocalDateTime now) {
        validateCardNumber(card.cardNumber());
        validateExpiry(card.expiryMonth(), card.expiryYear(), now);
        validateCvv(card.cvv());
    }

    /** AC2: 16 digits (spaces between the groups are allowed) and valid according to Luhn. */
    private void validateCardNumber(String cardNumber) {
        String digits = removeSpaces(cardNumber);
        if (!digits.matches(CARD_NUMBER_REGEX) || !passesLuhnCheck(digits)) {
            throw new PaymentException(INVALID_CARD_NUMBER);
        }
    }

    /** AC3: a card can be used until the end of its expiry month. */
    private void validateExpiry(int month, int year, LocalDateTime now) {
        if (YearMonth.of(year, month).isBefore(YearMonth.from(now))) {
            throw new PaymentException(CARD_EXPIRED);
        }
    }

    /** AC3: exactly 3 digits. */
    private void validateCvv(String cvv) {
        if (cvv == null || !cvv.matches(CVV_REGEX)) {
            throw new PaymentException(INVALID_CVV);
        }
    }

    /** AC5: every seat must still be in the customer's cart (locked by them, 10 minutes not passed). */
    private void validateReservation(Order order, LocalDateTime now) {
        boolean allReserved = order.getSeats().stream()
                .allMatch(seat -> isReservedFor(seat, order.getUserId(), now));
        if (!allReserved) {
            throw new PaymentException(RESERVATION_EXPIRED);
        }
    }

    /** AC4: the seats become SOLD (no longer locked) and the order becomes PAID. */
    private void markAsPaid(Order order) {
        order.getSeats().forEach(this::markAsSold);
        order.setStatus(OrderStatus.PAID);
    }

    private void markAsSold(Seat seat) {
        seat.setStatus(SeatStatus.SOLD);
        seat.setLockedUntil(null);
        seat.setLockedByUserId(null);
    }

    private boolean isReservedFor(Seat seat, Long userId, LocalDateTime now) {
        return seat.getStatus() == SeatStatus.LOCKED
                && Objects.equals(seat.getLockedByUserId(), userId)
                && now.isBefore(seat.getLockedUntil());
    }

    /**
     * Luhn algorithm: starting from the last digit, every second digit is doubled
     * (minus 9 if the result is bigger than 9); the sum of all digits must be divisible by 10.
     */
    private boolean passesLuhnCheck(String digits) {
        int sum = 0;
        for (int i = 0; i < digits.length(); i++) {
            int digit = Character.getNumericValue(digits.charAt(digits.length() - 1 - i));
            if (isSecondFromTheRight(i)) {
                digit = doubleLuhnDigit(digit);
            }
            sum += digit;
        }
        return sum % 10 == 0;
    }

    private boolean isSecondFromTheRight(int positionFromTheRight) {
        return positionFromTheRight % 2 == 1;
    }

    private int doubleLuhnDigit(int digit) {
        int doubled = digit * 2;
        return doubled > 9 ? doubled - 9 : doubled;
    }

    private String removeSpaces(String cardNumber) {
        return cardNumber == null ? "" : cardNumber.replace(" ", "");
    }
}
