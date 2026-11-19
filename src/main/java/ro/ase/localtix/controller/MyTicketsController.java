package ro.ase.localtix.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.ase.localtix.model.Ticket;
import ro.ase.localtix.repository.SeatRepository;
import ro.ase.localtix.repository.TicketRepository;
import ro.ase.localtix.service.TicketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * "My Tickets" page (US-12): the electronic tickets of the logged-in customer.
 * Each active ticket has a "Cancel ticket" button (US-10): the refund rules are applied by RefundService
 * (through TicketService.cancel), the seat becomes available again and the ticket is marked "Cancelled".
 */
@Controller
public class MyTicketsController {

    public static final String TICKET_NOT_FOUND = "Ticket not found";

    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final TicketService ticketService = new TicketService();

    public MyTicketsController(TicketRepository ticketRepository, SeatRepository seatRepository) {
        this.ticketRepository = ticketRepository;
        this.seatRepository = seatRepository;
    }

    @GetMapping("/my-tickets")
    public String showMyTickets(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("tickets",
                ticketService.getTicketsForCustomer(ticketRepository.findByUserId(userId), userId));
        return "my-tickets";
    }

    /** US-10: the customer cancels a ticket (cancelledByOrganizer = false). */
    @PostMapping("/my-tickets/{ticketId}/cancel")
    public String cancelTicket(@PathVariable Long ticketId, HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            Ticket ticket = findOwnTicket(ticketId, userId);
            BigDecimal refund = ticketService.cancel(ticket, false, LocalDateTime.now());
            seatRepository.save(ticket.getSeat()); // the seat is AVAILABLE again
            ticketRepository.save(ticket);         // CANCELLED + refunded amount
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ticket cancelled. Refunded amount: " + refund.toPlainString() + " RON");
        } catch (IllegalArgumentException e) {
            // e.g. RefundNotAllowedException: the event has already started
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-tickets";
    }

    /** A customer can only cancel their own tickets. */
    private Ticket findOwnTicket(Long ticketId, Long userId) {
        return ticketRepository.findById(ticketId)
                .filter(ticket -> Objects.equals(ticket.getUserId(), userId))
                .orElseThrow(() -> new IllegalArgumentException(TICKET_NOT_FOUND));
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(ProfileController.SESSION_USER_ID);
    }
}
