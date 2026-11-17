package ro.ase.localtix.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.service.EventCatalogService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class EventCatalogController {

    private final EventCatalogService eventCatalogService;

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
                    return "event-detail";
                })
                .orElse("redirect:/events");
    }
}
