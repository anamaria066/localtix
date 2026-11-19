package ro.ase.localtix.service;

import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.Ticket;
import ro.ase.localtix.model.TicketStatus;
import ro.ase.localtix.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Electronic tickets (US-12): unique codes, ticket creation after payment, "My Tickets", cancellation.
 * Plain Java: receives the current time and the codes already used as parameters.
 */
public class TicketService {


    public static final String CODE_PREFIX = "LTX";
    public static final int CODE_RANDOM_LENGTH = 6;
    public static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String CODE_SEPARATOR = "-";

    public static final String TICKET_ALREADY_CANCELLED = "This ticket has already been cancelled";


    private static final Comparator<Ticket> BY_EVENT_DATE =
            Comparator.comparing(ticket -> ticket.getSeat().getEvent().getDateTime());

    private final Random random;
    private final RefundService refundService = new RefundService();

    public TicketService() {
        this(new Random());
    }


    public TicketService(Random random) {
        this.random = random;
    }


    public String generateCode(LocalDateTime now, Set<String> existingCodes) {
        String code;
        do {
            code = buildCode(now.getYear());
        } while (existingCodes.contains(code));
        return code;
    }

    public Ticket createTicket(Seat seat, User customer, BigDecimal paidPrice,
                               Set<String> existingCodes, LocalDateTime now) {
        Ticket ticket = new Ticket();
        ticket.setCode(generateCode(now, existingCodes));
        ticket.setSeat(seat);
        ticket.setUserId(customer.getId());
        ticket.setCustomerName(fullName(customer));
        ticket.setPaidPrice(paidPrice);
        ticket.setStatus(TicketStatus.ACTIVE);
        ticket.setCreatedAt(now);
        return ticket;
    }

    public List<Ticket> createTickets(List<CartItem> items, User customer,
                                      Set<String> existingCodes, LocalDateTime now) {
        Set<String> usedCodes = new HashSet<>(existingCodes);
        List<Ticket> tickets = new ArrayList<>();
        for (CartItem item : items) {
            Ticket ticket = createTicket(item.seat(), customer, item.price(), usedCodes, now);
            usedCodes.add(ticket.getCode());
            tickets.add(ticket);
        }
        return tickets;
    }

    public List<Ticket> getTicketsForCustomer(List<Ticket> tickets, Long userId) {
        return tickets.stream()
                .filter(ticket -> userId.equals(ticket.getUserId()))
                .sorted(BY_EVENT_DATE)
                .toList();
    }

    /**
     * AC4 (+ US-10): cancels the ticket, stores and returns the refunded amount.
     * The refund rules (48h, 10% fee, event started) are applied by RefundService.
     *
     * @throws TicketAlreadyCancelledException if the ticket is already cancelled
     * @throws RefundNotAllowedException       if the event has started (the ticket stays active)
     */
    public BigDecimal cancel(Ticket ticket, boolean cancelledByOrganizer, LocalDateTime now) {
        validateNotCancelled(ticket);
        BigDecimal refund = refundService.cancel(ticket.getSeat(), ticket.getPaidPrice(), cancelledByOrganizer, now);
        markAsCancelled(ticket, refund);
        return refund;
    }


    public String getStatusLabel(Ticket ticket) {
        return ticket.getStatus().getDisplayName();
    }


    private String buildCode(int year) {
        return CODE_PREFIX + CODE_SEPARATOR + year + CODE_SEPARATOR + randomPart();
    }

    private String randomPart() {
        StringBuilder part = new StringBuilder(CODE_RANDOM_LENGTH);
        for (int i = 0; i < CODE_RANDOM_LENGTH; i++) {
            part.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
        }
        return part.toString();
    }

    private String fullName(User customer) {
        return customer.getFirstName() + " " + customer.getLastName();
    }

    private void validateNotCancelled(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new TicketAlreadyCancelledException(TICKET_ALREADY_CANCELLED);
        }
    }

    private void markAsCancelled(Ticket ticket, BigDecimal refund) {
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setRefundedAmount(refund);
    }
}
