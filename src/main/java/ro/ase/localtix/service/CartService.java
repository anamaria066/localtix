package ro.ase.localtix.service;

import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;
import ro.ase.localtix.model.TicketCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Cart logic (US-9).
 * The cart = the seats LOCKED by the customer whose lock has not expired yet.
 * The lock duration (10 minutes) is SeatReservationService.LOCK_DURATION_MINUTES.
 * Plain Java: receives the seats, the customer's id and the current time as parameters.
 */
public class CartService {

    public static final String NOT_IN_CART = "This ticket is not in your cart";

    private static final int PRICE_DECIMALS = 2;
    private static final BigDecimal EMPTY_TOTAL = BigDecimal.ZERO.setScale(PRICE_DECIMALS);

    private final PriceCalculator priceCalculator = new PriceCalculator();
    private final SeatReservationService seatReservationService = new SeatReservationService();
    private final PurchaseLimitsValidator purchaseLimitsValidator = new PurchaseLimitsValidator();

    /** AC1: the seats that are in the customer's cart at the moment "now". */
    public List<Seat> getCartSeats(List<Seat> seats, Long userId, LocalDateTime now) {
        return seats.stream()
                .filter(seat -> isInCart(seat, userId, now))
                .toList();
    }

    /**
     * Adds a seat of the event to the customer's cart (locks it for 10 minutes).
     *
     * @param eventSeats all the seats of the event (used to count the tickets already in the cart)
     * @throws PurchaseLimitException   if the cart would have more than 6 tickets (US-8)
     * @throws SeatUnavailableException if the seat is sold or locked by someone else (US-7)
     */
    public void addTicket(List<Seat> eventSeats, Seat seat, Long userId, LocalDateTime now) {
        int ticketsInCart = getCartSeats(eventSeats, userId, now).size();
        purchaseLimitsValidator.validateTicketCount(ticketsInCart + 1);
        seatReservationService.reserve(seat, userId, now);
    }

    /**
     * AC2: removes a ticket from the customer's cart and frees the seat.
     *
     * @throws IllegalArgumentException if the seat is not in this customer's cart
     */
    public void removeTicket(Seat seat, Long userId, LocalDateTime now) {
        if (!isInCart(seat, userId, now)) {
            throw new IllegalArgumentException(NOT_IN_CART);
        }
        seatReservationService.release(seat);
    }

    /** AC1: the final price of one ticket - the discount rules stay in PriceCalculator. */
    public BigDecimal calculateTicketPrice(Event event, TicketCategory category, LocalDateTime now) {
        return priceCalculator.calculate(event.getBasePrice(), category, daysBefore(event, now));
    }

    /** AC1: the total of the cart = the sum of the ticket prices, with 2 decimals. */
    public BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(CartItem::price)
                .reduce(EMPTY_TOTAL, BigDecimal::add)
                .setScale(PRICE_DECIMALS, RoundingMode.HALF_UP);
    }

    /** AC3: seconds left until the first seat in the cart is released (never negative). */
    public long getRemainingSeconds(List<Seat> cartSeats, LocalDateTime now) {
        return cartSeats.stream()
                .mapToLong(seat -> secondsUntilReleased(seat, now))
                .min()
                .orElse(0);
    }

    /** LOCKED by this customer and the 10 minutes have not passed yet. */
    private boolean isInCart(Seat seat, Long userId, LocalDateTime now) {
        return seat.getStatus() == SeatStatus.LOCKED
                && Objects.equals(seat.getLockedByUserId(), userId)
                && now.isBefore(seat.getLockedUntil());
    }

    private long secondsUntilReleased(Seat seat, LocalDateTime now) {
        long seconds = Duration.between(now, seat.getLockedUntil()).getSeconds();
        return Math.max(0, seconds);
    }

    /** Whole days between now and the event, as on the event page (US-6). */
    private long daysBefore(Event event, LocalDateTime now) {
        return ChronoUnit.DAYS.between(now, event.getDateTime());
    }
}
