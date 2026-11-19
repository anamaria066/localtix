package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;
import ro.ase.localtix.model.Ticket;
import ro.ase.localtix.model.TicketCategory;
import ro.ase.localtix.model.TicketStatus;
import ro.ase.localtix.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {

    private static final String CODE_FORMAT_2026 = "LTX-2026-[A-Z0-9]{6}";
    private static final String ALREADY_CANCELLED = "This ticket has already been cancelled";
    private static final BigDecimal PAID = new BigDecimal("150.00");
    private static final long SEED = 42;

    private final LocalDateTime now = LocalDateTime.of(2026, 11, 19, 10, 0);
    private final LocalDateTime eventStart = LocalDateTime.of(2026, 12, 15, 20, 0);

    private TicketService service;
    private Event event;
    private User maria;

    @BeforeEach
    void setUp() {
        service = new TicketService(new Random(SEED));
        event = new Event("Metrophonic Live", EventCategory.CONCERT, "București", "Sala Palatului",
                eventStart, PAID, "Rock concert");
        maria = new User("Ionescu", "Maria", "maria@localtix.ro", "0723456789", LocalDate.of(2002, 11, 21));
        maria.setId(2L);
    }

    private Seat soldSeat(Event event, String row, int number) {
        return new Seat(event, row, number, SeatStatus.SOLD);
    }

    private Ticket ticketOf(Long userId, Event event) {
        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setSeat(soldSeat(event, "A", 1));
        ticket.setPaidPrice(PAID);
        return ticket;
    }

    // --- AC1: each paid ticket gets a unique code LTX-YYYY-XXXXXX ---

    // AC1: the code has the format LTX-2026-XXXXXX (6 capital letters / digits)
    @Test
    void generatedCodeHasFormatLtxYearAndSixCharacters() {
        String code = service.generateCode(now, Set.of());

        assertNotNull(code);
        assertTrue(code.matches(CODE_FORMAT_2026), "Unexpected code: " + code);
    }

    // AC1: the year in the code is the year of the purchase
    @Test
    void codeContainsYearOfPurchase() {
        String code = service.generateCode(LocalDateTime.of(2027, 1, 5, 12, 0), Set.of());

        assertNotNull(code);
        assertTrue(code.matches("LTX-2027-[A-Z0-9]{6}"), "Unexpected code: " + code);
    }

    // AC1: a code that already exists is never generated again
    // (same seed -> the first code would be the same, so the service must choose another one)
    @Test
    void codeAlreadyUsedIsNotGeneratedAgain() {
        String firstCode = new TicketService(new Random(SEED)).generateCode(now, Set.of());
        assertNotNull(firstCode);

        String secondCode = new TicketService(new Random(SEED)).generateCode(now, Set.of(firstCode));

        assertNotNull(secondCode);
        assertNotEquals(firstCode, secondCode);
        assertTrue(secondCode.matches(CODE_FORMAT_2026), "Unexpected code: " + secondCode);
    }

    // AC1: 1000 generated codes are all different
    @Test
    void manyGeneratedCodesAreAllUnique() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String code = service.generateCode(now, codes);
            assertNotNull(code);
            codes.add(code);
        }

        assertEquals(1000, codes.size());
    }

    // AC1: after payment, every ticket of the order gets its own code
    @Test
    void eachPaidTicketGetsItsOwnCode() {
        List<CartItem> items = List.of(
                new CartItem(soldSeat(event, "A", 1), TicketCategory.NORMAL, PAID),
                new CartItem(soldSeat(event, "A", 2), TicketCategory.NORMAL, PAID),
                new CartItem(soldSeat(event, "A", 3), TicketCategory.NORMAL, PAID));

        List<Ticket> tickets = service.createTickets(items, maria, Set.of(), now);

        assertEquals(3, tickets.size());
        Set<String> codes = new HashSet<>();
        for (Ticket ticket : tickets) {
            assertNotNull(ticket.getCode());
            assertTrue(ticket.getCode().matches(CODE_FORMAT_2026), "Unexpected code: " + ticket.getCode());
            codes.add(ticket.getCode());
        }
        assertEquals(3, codes.size());
    }

    // --- AC2: the ticket contains the event, date, location, seat, customer name and code ---

    // AC2: event, date and location (through the seat), seat, customer name and code
    @Test
    void ticketContainsEventSeatCustomerNameAndCode() {
        Seat seat = soldSeat(event, "B", 4);

        Ticket ticket = service.createTicket(seat, maria, PAID, Set.of(), now);

        assertSame(seat, ticket.getSeat());
        assertEquals("Metrophonic Live", ticket.getSeat().getEvent().getTitle());
        assertEquals(eventStart, ticket.getSeat().getEvent().getDateTime());
        assertEquals("Sala Palatului", ticket.getSeat().getEvent().getLocation());
        assertEquals("Maria Ionescu", ticket.getCustomerName());
        assertEquals(2L, ticket.getUserId());
        assertNotNull(ticket.getCode());
        assertTrue(ticket.getCode().matches(CODE_FORMAT_2026), "Unexpected code: " + ticket.getCode());
    }

    // AC2: a new ticket is active and keeps the price paid (needed for the refund, US-10)
    @Test
    void newTicketIsActiveAndKeepsPaidPrice() {
        Ticket ticket = service.createTicket(soldSeat(event, "A", 1), maria,
                new BigDecimal("105.00"), Set.of(), now);

        assertEquals(new BigDecimal("105.00"), ticket.getPaidPrice());
        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(now, ticket.getCreatedAt());
        assertNull(ticket.getRefundedAmount());
    }

    // AC2: each ticket of the order keeps its own seat and price (e.g. Normal and Student)
    @Test
    void createdTicketsKeepSeatAndPriceOfEachItem() {
        Seat seatA1 = soldSeat(event, "A", 1);
        Seat seatA2 = soldSeat(event, "A", 2);
        List<CartItem> items = List.of(
                new CartItem(seatA1, TicketCategory.NORMAL, new BigDecimal("150.00")),
                new CartItem(seatA2, TicketCategory.STUDENT, new BigDecimal("105.00")));

        List<Ticket> tickets = service.createTickets(items, maria, Set.of(), now);

        assertEquals(2, tickets.size());
        assertSame(seatA1, tickets.get(0).getSeat());
        assertEquals(new BigDecimal("150.00"), tickets.get(0).getPaidPrice());
        assertSame(seatA2, tickets.get(1).getSeat());
        assertEquals(new BigDecimal("105.00"), tickets.get(1).getPaidPrice());
    }

    // --- AC3: the tickets appear in the customer's account ("My Tickets") ---

    // AC3: the customer sees only their own tickets
    @Test
    void customerSeesOnlyOwnTickets() {
        Ticket mine = ticketOf(2L, event);
        Ticket someoneElses = ticketOf(1L, event);

        List<Ticket> myTickets = service.getTicketsForCustomer(List.of(mine, someoneElses), 2L);

        assertEquals(List.of(mine), myTickets);
    }

    // AC3: "My Tickets" shows the soonest event first
    @Test
    void customerTicketsAreSortedByEventDate() {
        Event later = new Event("Riverside Tour", EventCategory.CONCERT, "Iași", "Sala Sporturilor",
                LocalDateTime.of(2027, 1, 10, 20, 0), PAID, "Concert");
        Event sooner = new Event("Hamlet", EventCategory.THEATRE, "București", "Teatrul Național",
                LocalDateTime.of(2026, 11, 25, 18, 0), PAID, "Theatre");
        Ticket laterTicket = ticketOf(2L, later);
        Ticket middleTicket = ticketOf(2L, event);
        Ticket soonerTicket = ticketOf(2L, sooner);

        List<Ticket> myTickets = service.getTicketsForCustomer(
                List.of(laterTicket, middleTicket, soonerTicket), 2L);

        assertEquals(List.of(soonerTicket, middleTicket, laterTicket), myTickets);
    }

    // AC3: a customer without tickets gets an empty list
    @Test
    void customerWithoutTicketsGetsEmptyList() {
        List<Ticket> myTickets = service.getTicketsForCustomer(List.of(ticketOf(1L, event)), 2L);

        assertTrue(myTickets.isEmpty());
    }

    // --- AC4: cancelled tickets are marked "Cancelled" ---

    // AC4: an active ticket is shown as "Active"
    @Test
    void activeTicketIsLabelledActive() {
        Ticket ticket = ticketOf(2L, event);

        assertEquals("Active", service.getStatusLabel(ticket));
    }

    // AC4: after cancellation the ticket is CANCELLED and shown as "Cancelled"
    @Test
    void cancelledTicketIsMarkedCancelled() {
        Ticket ticket = ticketOf(2L, event);

        service.cancel(ticket, false, eventStart.minusDays(3));

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        assertEquals("Cancelled", service.getStatusLabel(ticket));
    }

    // AC4 + US-10: the refund (RefundService rules: 90% more than 48h before) is returned and kept on the ticket
    @Test
    void cancellationStoresRefundedAmountOnTicket() {
        Ticket ticket = ticketOf(2L, event);

        BigDecimal refund = service.cancel(ticket, false, eventStart.minusDays(3));

        assertEquals(new BigDecimal("135.00"), refund);
        assertEquals(new BigDecimal("135.00"), ticket.getRefundedAmount());
    }

    // AC4 + US-10: the seat of the cancelled ticket becomes available again
    @Test
    void cancellationFreesTheSeat() {
        Ticket ticket = ticketOf(2L, event);

        service.cancel(ticket, false, eventStart.minusDays(3));

        assertEquals(SeatStatus.AVAILABLE, ticket.getSeat().getStatus());
    }

    // AC4: a cancelled ticket cannot be cancelled a second time
    @Test
    void cancelledTicketCannotBeCancelledAgain() {
        Ticket ticket = ticketOf(2L, event);
        ticket.setStatus(TicketStatus.CANCELLED);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.cancel(ticket, false, eventStart.minusDays(3)));

        assertEquals(ALREADY_CANCELLED, ex.getMessage());
    }

    // AC4 + US-10: if the event has started, the cancellation is refused and the ticket stays active
    @Test
    void ticketStaysActiveWhenEventAlreadyStarted() {
        Ticket ticket = ticketOf(2L, event);

        assertThrows(RefundNotAllowedException.class,
                () -> service.cancel(ticket, false, eventStart.plusHours(1)));

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertNull(ticket.getRefundedAmount());
    }
}
