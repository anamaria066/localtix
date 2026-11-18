package ro.ase.localtix.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;
import ro.ase.localtix.repository.SeatRepository;
import ro.ase.localtix.service.EventCatalogService;
import ro.ase.localtix.service.SeatReservationService;
import ro.ase.localtix.service.SeatUnavailableException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Seat map of an event (US-7): shows the seats and lets a logged-in customer reserve one.
 */
@Controller
public class SeatMapController {

    public static final String SEAT_NOT_FOUND = "This seat does not exist";
    public static final String LOGIN_REQUIRED = "Please log in to choose your seats.";

    private final EventCatalogService eventCatalogService;
    private final SeatRepository seatRepository;
    private final SeatReservationService seatReservationService = new SeatReservationService();

    public SeatMapController(EventCatalogService eventCatalogService, SeatRepository seatRepository) {
        this.eventCatalogService = eventCatalogService;
        this.seatRepository = seatRepository;
    }

    @GetMapping("/events/{id}/seats")
    public String showSeatMap(@PathVariable Long id, HttpSession session, Model model) {
        Optional<Event> event = eventCatalogService.findById(id);
        if (event.isEmpty()) {
            return "redirect:/events";
        }
        LocalDateTime now = LocalDateTime.now();
        Long userId = currentUserId(session);
        List<Seat> seats = loadSeatsReleasingExpiredLocks(id, now);

        List<SeatView> seatViews = seats.stream()
                .sorted(Comparator.comparing(Seat::getRowLabel).thenComparing(Seat::getSeatNumber))
                .map(seat -> toView(seat, userId, now))
                .toList();
        Map<String, List<SeatView>> rows = seatViews.stream()
                .collect(Collectors.groupingBy(SeatView::rowLabel, LinkedHashMap::new, Collectors.toList()));

        model.addAttribute("event", event.get());
        model.addAttribute("rows", rows);
        model.addAttribute("mySeats", seatViews.stream().filter(SeatView::mine).toList());
        model.addAttribute("loggedIn", userId != null);
        model.addAttribute("lockMinutes", SeatReservationService.LOCK_DURATION_MINUTES);
        return "seats";
    }

    @PostMapping("/events/{id}/seats")
    public String reserveSeat(@PathVariable Long id, @RequestParam Long seatId,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", LOGIN_REQUIRED);
            return "redirect:/login";
        }
        try {
            Seat seat = lockSeat(id, seatId, userId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Seat " + label(seat) + " has been added to your cart. It is reserved for you for "
                            + SeatReservationService.LOCK_DURATION_MINUTES + " minutes.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + id + "/seats";
    }

    /**
     * Reads the seat, reserves it and saves it.
     * "synchronized": if two customers click the same seat at the same time, they are handled
     * one after the other, so only the first one gets it and the second one sees the error (AC5).
     */
    private synchronized Seat lockSeat(Long eventId, Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .filter(s -> s.getEvent().getId().equals(eventId))
                .orElseThrow(() -> new SeatUnavailableException(SEAT_NOT_FOUND));
        seatReservationService.reserve(seat, userId, LocalDateTime.now());
        return seatRepository.save(seat);
    }

    /** AC4: seats not paid within 10 minutes are freed before the map is shown. */
    private List<Seat> loadSeatsReleasingExpiredLocks(Long eventId, LocalDateTime now) {
        List<Seat> seats = seatRepository.findByEventId(eventId);
        seatReservationService.releaseExpiredLocks(seats, now);
        return seatRepository.saveAll(seats);
    }

    private SeatView toView(Seat seat, Long userId, LocalDateTime now) {
        SeatStatus status = seatReservationService.getDisplayStatus(seat, now);
        boolean mine = status == SeatStatus.LOCKED && userId != null
                && Objects.equals(seat.getLockedByUserId(), userId);
        String cssClass = mine ? "lt-seat-selected" : "lt-seat-" + status.name().toLowerCase();
        return new SeatView(seat.getId(), seat.getRowLabel(), seat.getSeatNumber(), label(seat),
                cssClass, tooltip(status, mine), status == SeatStatus.AVAILABLE, mine,
                mine ? seat.getLockedUntil() : null);
    }

    private String tooltip(SeatStatus status, boolean mine) {
        if (mine) {
            return "In your cart";
        }
        return switch (status) {
            case AVAILABLE -> "Available";
            case LOCKED -> "Reserved by another customer";
            case SOLD -> "Sold";
        };
    }

    private String label(Seat seat) {
        return seat.getRowLabel() + seat.getSeatNumber();
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(ProfileController.SESSION_USER_ID);
    }

    /** One seat on the map. */
    public record SeatView(Long id, String rowLabel, int number, String label, String cssClass,
                           String tooltip, boolean selectable, boolean mine, LocalDateTime lockedUntil) {
    }
}
