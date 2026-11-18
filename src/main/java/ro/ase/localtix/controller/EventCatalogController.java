package ro.ase.localtix.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;
import ro.ase.localtix.model.TicketCategory;
import ro.ase.localtix.service.EventCatalogService;
import ro.ase.localtix.service.EventFilter;
import ro.ase.localtix.service.EventFilterService;
import ro.ase.localtix.service.PriceCalculator;
import ro.ase.localtix.service.PurchaseLimitsValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class EventCatalogController {

    private final EventCatalogService eventCatalogService;
    private final EventFilterService eventFilterService;
    private final PriceCalculator priceCalculator = new PriceCalculator();
    private final PurchaseLimitsValidator purchaseLimitsValidator = new PurchaseLimitsValidator();

    public EventCatalogController(EventCatalogService eventCatalogService,
                                  EventFilterService eventFilterService) {
        this.eventCatalogService = eventCatalogService;
        this.eventFilterService = eventFilterService;
    }

    /** US-4 catalog + US-5 filters (all filter parameters are optional). */
    @GetMapping("/events")
    public String showCatalog(@RequestParam(required = false) EventCategory category,
                              @RequestParam(required = false) String city,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                              @RequestParam(required = false) BigDecimal minPrice,
                              @RequestParam(required = false) BigDecimal maxPrice,
                              Model model) {
        List<Event> upcoming = eventCatalogService.getUpcomingEvents(LocalDateTime.now());
        EventFilter filter = new EventFilter(category, city, dateFrom, dateTo, minPrice, maxPrice);

        List<Event> events;
        try {
            events = eventFilterService.filter(upcoming, filter);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            events = upcoming;
        }

        Map<Long, Long> seatCounts = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> eventCatalogService.countAvailableSeats(e.getId())
                ));
        model.addAttribute("events", events);
        model.addAttribute("seatCounts", seatCounts);
        model.addAttribute("filter", filter);
        model.addAttribute("filtersApplied", isAnyFilterSet(filter));
        model.addAttribute("categories", EventCategory.values());
        model.addAttribute("cities", upcoming.stream().map(Event::getCity).distinct().sorted().toList());
        return "events";
    }

    private boolean isAnyFilterSet(EventFilter filter) {
        return filter.category() != null
                || (filter.city() != null && !filter.city().isBlank())
                || filter.dateFrom() != null || filter.dateTo() != null
                || filter.minPrice() != null || filter.maxPrice() != null;
    }

    @GetMapping("/events/{id}")
    public String showEventDetail(@PathVariable Long id, Model model) {
        return eventCatalogService.findById(id)
                .map(event -> {
                    long availableSeats = eventCatalogService.countAvailableSeats(id);
                    model.addAttribute("event", event);
                    model.addAttribute("availableSeats", availableSeats);
                    // US-8 AC2: the buy button is inactive when the event is sold out
                    model.addAttribute("soldOut", purchaseLimitsValidator.isSoldOut(availableSeats));
                    addPrices(event, model);
                    return "event-detail";
                })
                .orElse("redirect:/events");
    }

    /** US-6: original price, applied discount and final price for every ticket category. */
    private void addPrices(Event event, Model model) {
        long daysBefore = ChronoUnit.DAYS.between(LocalDateTime.now(), event.getDateTime());

        List<TicketPrice> prices = Arrays.stream(TicketCategory.values())
                .map(category -> new TicketPrice(
                        category.getDisplayName(),
                        event.getBasePrice(),
                        priceCalculator.getDiscountPercent(category, daysBefore),
                        discountReason(category, daysBefore),
                        priceCalculator.calculate(event.getBasePrice(), category, daysBefore)))
                .toList();

        model.addAttribute("prices", prices);
        model.addAttribute("earlyBird", daysBefore > PriceCalculator.EARLY_BIRD_MIN_DAYS);
        model.addAttribute("earlyBirdPercent", PriceCalculator.EARLY_BIRD_DISCOUNT_PERCENT);
        model.addAttribute("earlyBirdDays", PriceCalculator.EARLY_BIRD_MIN_DAYS);
    }

    /** Which discount won: the category one or early bird (only the biggest one applies). */
    private String discountReason(TicketCategory category, long daysBefore) {
        int discount = priceCalculator.getDiscountPercent(category, daysBefore);
        if (discount == 0) {
            return "No discount";
        }
        return discount == category.getDiscountPercent()
                ? category.getDisplayName() + " discount"
                : "Early bird";
    }

    /** One row of the price table on the event page. */
    public record TicketPrice(String category, BigDecimal originalPrice, int discountPercent,
                              String discountReason, BigDecimal finalPrice) {
    }
}
