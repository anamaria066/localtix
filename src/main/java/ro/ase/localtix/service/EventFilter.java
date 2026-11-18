package ro.ase.localtix.service;

import ro.ase.localtix.model.EventCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filter criteria for the event catalog (US-5).
 * A null (or blank) value means "this filter is not applied".
 */
public record EventFilter(EventCategory category,
                          String city,
                          LocalDate dateFrom,
                          LocalDate dateTo,
                          BigDecimal minPrice,
                          BigDecimal maxPrice) {
}
