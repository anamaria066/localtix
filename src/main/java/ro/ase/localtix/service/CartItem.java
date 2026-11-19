package ro.ase.localtix.service;

import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.TicketCategory;

import java.math.BigDecimal;

/**
 * One ticket in the cart (US-9): the seat (with its event), the ticket category and the final price.
 */
public record CartItem(Seat seat, TicketCategory category, BigDecimal price) {
}
