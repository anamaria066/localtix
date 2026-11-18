package ro.ase.localtix.service;

import org.springframework.stereotype.Service;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * US-5: filters the event catalog by category, city, date interval and price interval.
 * Every filter left empty (null / blank) is skipped, so the filters can be freely combined.
 */
@Service
public class EventFilterService {

    public static final String INVALID_DATE_INTERVAL = "Start date cannot be after end date";
    public static final String INVALID_PRICE_INTERVAL = "Minimum price cannot be greater than maximum price";

    /** Returns the events that match all the given filters, keeping their original order. */
    public List<Event> filter(List<Event> events, EventFilter filter) {
        validateIntervals(filter);

        return events.stream()
                .filter(event -> matchesCategory(event, filter.category()))
                .filter(event -> matchesCity(event, filter.city()))
                .filter(event -> matchesDateInterval(event, filter.dateFrom(), filter.dateTo()))
                .filter(event -> matchesPriceInterval(event, filter.minPrice(), filter.maxPrice()))
                .toList();
    }

    private void validateIntervals(EventFilter filter) {
        if (filter.dateFrom() != null && filter.dateTo() != null
                && filter.dateFrom().isAfter(filter.dateTo())) {
            throw new IllegalArgumentException(INVALID_DATE_INTERVAL);
        }
        if (filter.minPrice() != null && filter.maxPrice() != null
                && filter.minPrice().compareTo(filter.maxPrice()) > 0) {
            throw new IllegalArgumentException(INVALID_PRICE_INTERVAL);
        }
    }

    private boolean matchesCategory(Event event, EventCategory category) {
        return category == null || event.getCategory() == category;
    }

    private boolean matchesCity(Event event, String city) {
        if (city == null || city.isBlank()) {
            return true;
        }
        return event.getCity().equalsIgnoreCase(city.trim());
    }

    /** Compares only the day of the event; both ends of the interval are included. */
    private boolean matchesDateInterval(Event event, LocalDate dateFrom, LocalDate dateTo) {
        LocalDate eventDay = event.getDateTime().toLocalDate();
        boolean afterStart = dateFrom == null || !eventDay.isBefore(dateFrom);
        boolean beforeEnd = dateTo == null || !eventDay.isAfter(dateTo);
        return afterStart && beforeEnd;
    }

    /** Uses the base price (without discounts); both ends of the interval are included. */
    private boolean matchesPriceInterval(Event event, BigDecimal minPrice, BigDecimal maxPrice) {
        BigDecimal price = event.getBasePrice();
        boolean aboveMin = minPrice == null || price.compareTo(minPrice) >= 0;
        boolean belowMax = maxPrice == null || price.compareTo(maxPrice) <= 0;
        return aboveMin && belowMax;
    }
}
