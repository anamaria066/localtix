package ro.ase.localtix.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;
import ro.ase.localtix.model.TicketCategory;
import ro.ase.localtix.repository.SeatRepository;
import ro.ase.localtix.service.CartItem;
import ro.ase.localtix.service.CartService;
import ro.ase.localtix.service.SeatReservationService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final SeatRepository seatRepository;
    private final CartService cartService = new CartService();

    public CartController(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
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

    /** AC4: "Checkout" leads to the payment page (the card form comes with US-11). */
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
        return "checkout";
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
