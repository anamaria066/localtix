package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;
import ro.ase.localtix.repository.EventRepository;
import ro.ase.localtix.repository.SeatRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventCatalogServiceTest {

    private EventRepository eventRepository;
    private SeatRepository seatRepository;
    private EventCatalogService service;

    private final LocalDateTime NOW = LocalDateTime.of(2026, 11, 17, 12, 0);

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        seatRepository = mock(SeatRepository.class);
        service = new EventCatalogService(eventRepository, seatRepository);
    }

    // --- getUpcomingEvents ---

    // Criteriu 3: evenimentele trecute nu apar
    @Test
    void pastEventsAreExcluded() {
        Event past = eventWithDate(NOW.minusDays(1));
        when(eventRepository.findAll()).thenReturn(List.of(past));

        List<Event> result = service.getUpcomingEvents(NOW);

        assertTrue(result.isEmpty());
    }

    // Criteriu 3: evenimentele exacte cu acum nu apar
    @Test
    void eventStartingNowIsExcluded() {
        Event exact = eventWithDate(NOW);
        when(eventRepository.findAll()).thenReturn(List.of(exact));

        List<Event> result = service.getUpcomingEvents(NOW);

        assertTrue(result.isEmpty());
    }

    // Criteriu 3: evenimentele viitoare apar
    @Test
    void futureEventsAreIncluded() {
        Event future = eventWithDate(NOW.plusDays(1));
        when(eventRepository.findAll()).thenReturn(List.of(future));

        List<Event> result = service.getUpcomingEvents(NOW);

        assertEquals(1, result.size());
    }

    // Criteriu 2: sortare după dată, cele mai apropiate primele
    @Test
    void upcomingEventsAreSortedByDateAscending() {
        Event first = eventWithDate(NOW.plusDays(5));
        Event second = eventWithDate(NOW.plusDays(1));
        Event third = eventWithDate(NOW.plusDays(10));
        when(eventRepository.findAll()).thenReturn(List.of(first, second, third));

        List<Event> result = service.getUpcomingEvents(NOW);

        assertEquals(second, result.get(0));
        assertEquals(first, result.get(1));
        assertEquals(third, result.get(2));
    }

    // Mix de trecut și viitor — doar viitoarele apar, sortate
    @Test
    void mixOfPastAndFutureReturnsOnlyFutureSorted() {
        Event past = eventWithDate(NOW.minusDays(2));
        Event nearFuture = eventWithDate(NOW.plusDays(3));
        Event farFuture = eventWithDate(NOW.plusDays(30));
        when(eventRepository.findAll()).thenReturn(List.of(farFuture, past, nearFuture));

        List<Event> result = service.getUpcomingEvents(NOW);

        assertEquals(2, result.size());
        assertEquals(nearFuture, result.get(0));
        assertEquals(farFuture, result.get(1));
    }

    // --- countAvailableSeats ---

    // Criteriu 1: se numără doar locurile AVAILABLE (nu LOCKED sau SOLD)
    @Test
    void countsOnlyAvailableSeats() {
        Event event = eventWithId(1L);
        Seat available1 = seatWithStatus(event, SeatStatus.AVAILABLE);
        Seat available2 = seatWithStatus(event, SeatStatus.AVAILABLE);
        Seat sold = seatWithStatus(event, SeatStatus.SOLD);
        Seat locked = seatWithStatus(event, SeatStatus.LOCKED);
        when(seatRepository.findByEventId(1L)).thenReturn(List.of(available1, available2, sold, locked));

        long count = service.countAvailableSeats(1L);

        assertEquals(2, count);
    }

    // Eveniment fără niciun loc disponibil
    @Test
    void returnsZeroWhenNoAvailableSeats() {
        Event event = eventWithId(2L);
        Seat sold = seatWithStatus(event, SeatStatus.SOLD);
        when(seatRepository.findByEventId(2L)).thenReturn(List.of(sold));

        long count = service.countAvailableSeats(2L);

        assertEquals(0, count);
    }

    // Eveniment fără niciun loc — 0 disponibile
    @Test
    void returnsZeroWhenEventHasNoSeats() {
        when(seatRepository.findByEventId(99L)).thenReturn(List.of());

        long count = service.countAvailableSeats(99L);

        assertEquals(0, count);
    }

    // --- findById ---

    // Criteriu 4: click pe eveniment deschide pagina de detalii
    @Test
    void findByIdReturnsEventWhenExists() {
        Event event = eventWithId(1L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Optional<Event> result = service.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(event, result.get());
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Event> result = service.findById(999L);

        assertTrue(result.isEmpty());
    }

    // --- helpers ---

    private Event eventWithDate(LocalDateTime dateTime) {
        Event e = new Event("Test", EventCategory.CONCERT, "City", "Venue",
                dateTime, BigDecimal.valueOf(100), "Description");
        return e;
    }

    private Event eventWithId(Long id) {
        Event e = eventWithDate(NOW.plusDays(10));
        e.setId(id);
        return e;
    }

    private Seat seatWithStatus(Event event, SeatStatus status) {
        return new Seat(event, "A", 1, status);
    }
}