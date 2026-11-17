package ro.ase.localtix.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.TicketCategory;
import ro.ase.localtix.service.EventCatalogService;
import ro.ase.localtix.service.PriceCalculator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class EventCatalogController {

    private final EventCatalogService eventCatalogService;
    private final PriceCalculator priceCalculator = new PriceCalculator();

    public EventCatalogController(EventCatalogService eventCatalogService) {
        this.eventCatalogService = eventCatalogService;
    }

    @GetMapping("/events")
    public String showCatalog(Model model) {
        var events = eventCatalogService.getUpcomingEvents(LocalDateTime.now());
        Map<Long, Long> seatCounts = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> eventCatalogService.countAvailableSeats(e.getId())
                ));
        model.addAttribute("events", events);
        model.addAttribute("seatCounts", seatCounts);
        return "events";
    }

    @GetMapping("/events/{id}")
    public String showEventDetail(@PathVariable Long id, Model model) {
        return eventCatalogService.findById(id)
                .map(event -> {
                    model.addAttribute("event", event);
                    model.addAttribute("availableSeats",
                            eventCatalogService.countAvailableSeats(id));
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
