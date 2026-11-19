package ro.ase.localtix.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.ase.localtix.model.Order;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.Ticket;
import ro.ase.localtix.model.SeatStatus;
import ro.ase.localtix.model.TicketCategory;
import ro.ase.localtix.model.User;
import ro.ase.localtix.repository.OrderRepository;
import ro.ase.localtix.repository.SeatRepository;
import ro.ase.localtix.repository.TicketRepository;
import ro.ase.localtix.repository.UserRepository;
import ro.ase.localtix.service.CardDetails;
import ro.ase.localtix.service.CartItem;
import ro.ase.localtix.service.CartService;
import ro.ase.localtix.service.PaymentException;
import ro.ase.localtix.service.PaymentService;
import ro.ase.localtix.service.SeatReservationService;
import ro.ase.localtix.service.TicketService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Cart page (US-9): the seats in the customer's cart, the ticket category of each seat,
 * the prices, the total and the time left. "Checkout" leads to the payment page.
 *
 * The seats in the cart come from the database (LOCKED seats of the customer, US-7);
 * the session only remembers the ticket category chosen for each seat (default: Normal).
 */
@Controller
public class CartController {

    /** Session key: Map seatId -> TicketCategory chosen in the cart. */
    public static final String SESSION_CART_CATEGORIES = "cartCategories";

    public static final String CART_EMPTY = "Your cart is empty. Choose your seats first.";

    /** How many years after the current one are offered for the card expiry date. */
    private static final int EXPIRY_YEARS_SHOWN = 10;

    private final SeatRepository seatRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CartService cartService = new CartService();
    private final PaymentService paymentService = new PaymentService();
    private final TicketService ticketService = new TicketService();

    public CartController(SeatRepository seatRepository, OrderRepository orderRepository,
                          TicketRepository ticketRepository, UserRepository userRepository) {
        this.seatRepository = seatRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        addCartToModel(userId, session, model);
        model.addAttribute("categories", TicketCategory.values());
        return "cart";
    }

    /** AC1: the customer chooses the ticket category (Normal / Student / Senior) of a seat. */
    @PostMapping("/cart/category")
    public String changeCategory(@RequestParam Long seatId, @RequestParam TicketCategory category,
                                 HttpSession session) {
        if (currentUserId(session) == null) {
            return "redirect:/login";
        }
        getCategories(session).put(seatId, category);
        return "redirect:/cart";
    }

    /** AC2: removes a ticket from the cart; the seat becomes available again. */
    @PostMapping("/cart/remove")
    public String removeTicket(@RequestParam Long seatId, HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new IllegalArgumentException(CartService.NOT_IN_CART));
            cartService.removeTicket(seat, userId, LocalDateTime.now());
            seatRepository.save(seat);
            getCategories(session).remove(seatId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Seat " + label(seat) + " has been removed from your cart.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    /** AC4: "Checkout" leads to the payment page with the card form (US-11). */
    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        List<CartItem> items = addCartToModel(userId, session, model);
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", CART_EMPTY);
            return "redirect:/cart";
        }
        addCardFormOptions(model);
        return "checkout";
    }

    /**
     * US-11: simulated card payment of the seats in the cart.
     * Success: the order is saved as PAID, the seats become SOLD and the cart is emptied.
     * Failure: the error is shown on the payment page; the seats stay locked until the 10 minutes expire.
     */
    @PostMapping("/checkout/pay")
    public String pay(@RequestParam String cardNumber, @RequestParam int expiryMonth,
                      @RequestParam int expiryYear, @RequestParam String cvv,
                      HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        List<CartItem> items = addCartToModel(userId, session, model);
        if (items.isEmpty()) {
            // The 10 minutes have passed while the customer was on the payment page
            redirectAttributes.addFlashAttribute("errorMessage", PaymentService.RESERVATION_EXPIRED);
            return "redirect:/cart";
        }

        LocalDateTime now = LocalDateTime.now();
        List<Seat> seats = items.stream().map(CartItem::seat).toList();
        Order order = new Order(userId, seats, cartService.calculateTotal(items), now);
        try {
            paymentService.pay(order, new CardDetails(cardNumber, expiryMonth, expiryYear, cvv), now);
        } catch (PaymentException e) {
            // AC5: nothing is saved, the seats stay LOCKED; the form keeps what was typed (except the CVV)
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("cardNumber", cardNumber);
            model.addAttribute("selectedMonth", expiryMonth);
            model.addAttribute("selectedYear", expiryYear);
            addCardFormOptions(model);
            return "checkout";
        }

        // AC4: the seats are SOLD and the order is PAID
        seatRepository.saveAll(seats);
        orderRepository.save(order);
        createTickets(items, userId, now);
        getCategories(session).clear();
        return "redirect:/orders/" + order.getId();
    }

    /** US-12: one electronic ticket with a unique code for each paid seat, with the price paid. */
    private void createTickets(List<CartItem> items, Long userId, LocalDateTime now) {
        User customer = userRepository.findById(userId).orElseThrow();
        Set<String> existingCodes = new HashSet<>(ticketRepository.findAllCodes());
        List<Ticket> tickets = ticketService.createTickets(items, customer, existingCodes, now);
        ticketRepository.saveAll(tickets);
    }

    /** The options of the expiry date selects: months 1-12 and the next years. */
    private void addCardFormOptions(Model model) {
        int currentYear = LocalDateTime.now().getYear();
        model.addAttribute("months", IntStream.rangeClosed(1, 12).boxed().toList());
        model.addAttribute("expiryYears",
                IntStream.rangeClosed(currentYear, currentYear + EXPIRY_YEARS_SHOWN).boxed().toList());
    }

    /** Puts items, total and remaining time in the model; returns the items. */
    private List<CartItem> addCartToModel(Long userId, HttpSession session, Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<Seat> lockedSeats = seatRepository.findByStatusAndLockedByUserId(SeatStatus.LOCKED, userId);
        List<Seat> cartSeats = cartService.getCartSeats(lockedSeats, userId, now);

        Map<Long, TicketCategory> categories = getCategories(session);
        forgetSeatsNoLongerInCart(categories, cartSeats);

        List<CartItem> items = cartSeats.stream()
                .sorted(Comparator.comparing((Seat seat) -> seat.getEvent().getDateTime())
                        .thenComparing(Seat::getRowLabel)
                        .thenComparing(Seat::getSeatNumber))
                .map(seat -> toItem(seat, categories, now))
                .toList();

        model.addAttribute("items", items);
        model.addAttribute("total", cartService.calculateTotal(items));
        model.addAttribute("remainingSeconds", cartService.getRemainingSeconds(cartSeats, now));
        model.addAttribute("lockMinutes", SeatReservationService.LOCK_DURATION_MINUTES);
        return items;
    }

    private CartItem toItem(Seat seat, Map<Long, TicketCategory> categories, LocalDateTime now) {
        TicketCategory category = categories.getOrDefault(seat.getId(), TicketCategory.NORMAL);
        return new CartItem(seat, category, cartService.calculateTicketPrice(seat.getEvent(), category, now));
    }

    /** Seats removed or expired are forgotten, so a seat reserved again starts as Normal. */
    private void forgetSeatsNoLongerInCart(Map<Long, TicketCategory> categories, List<Seat> cartSeats) {
        Set<Long> cartSeatIds = cartSeats.stream().map(Seat::getId).collect(Collectors.toSet());
        categories.keySet().retainAll(cartSeatIds);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, TicketCategory> getCategories(HttpSession session) {
        Map<Long, TicketCategory> categories =
                (Map<Long, TicketCategory>) session.getAttribute(SESSION_CART_CATEGORIES);
        if (categories == null) {
            categories = new HashMap<>();
            session.setAttribute(SESSION_CART_CATEGORIES, categories);
        }
        return categories;
    }

    private String label(Seat seat) {
        return seat.getRowLabel() + seat.getSeatNumber();
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(ProfileController.SESSION_USER_ID);
    }
}
