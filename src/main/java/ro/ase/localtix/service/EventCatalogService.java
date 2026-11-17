package ro.ase.localtix.service;

import org.springframework.stereotype.Service;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;
import ro.ase.localtix.repository.EventRepository;
import ro.ase.localtix.repository.SeatRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class EventCatalogService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public EventCatalogService(EventRepository eventRepository, SeatRepository seatRepository) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    public List<Event> getUpcomingEvents(LocalDateTime now) {
        return eventRepository.findAll().stream()
                .filter(e -> isUpcoming(e, now))
                .sorted(Comparator.comparing(Event::getDateTime))
                .toList();
    }

    public long countAvailableSeats(Long eventId) {
        return seatRepository.findByEventId(eventId).stream()
                .filter(this::isAvailable)
                .count();
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    private boolean isUpcoming(Event event, LocalDateTime now) {
        return event.getDateTime().isAfter(now);
    }

    private boolean isAvailable(Seat seat) {
        return seat.getStatus() == SeatStatus.AVAILABLE;
    }
}