package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.Event;
import ro.ase.localtix.model.EventCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventFilterServiceTest {

    private EventFilterService service;

    private Event standupBucharestCheap;   // STANDUP, București, 80 RON, 10 Dec 2026
    private Event standupBucharestPricey;  // STANDUP, București, 150 RON, 12 Dec 2026
    private Event standupCluj;             // STANDUP, Cluj-Napoca, 60 RON, 15 Dec 2026
    private Event concertBucharest;        // CONCERT, București, 90 RON, 20 Dec 2026
    private Event theatreIasi;             // THEATRE, Iași, 100 RON, 05 Jan 2027

    private List<Event> allEvents;

    @BeforeEach
    void setUp() {
        service = new EventFilterService();

        standupBucharestCheap = event(EventCategory.STANDUP, "București", "80.00",
                LocalDateTime.of(2026, 12, 10, 20, 0));
        standupBucharestPricey = event(EventCategory.STANDUP, "București", "150.00",
                LocalDateTime.of(2026, 12, 12, 20, 0));
        standupCluj = event(EventCategory.STANDUP, "Cluj-Napoca", "60.00",
                LocalDateTime.of(2026, 12, 15, 19, 0));
        concertBucharest = event(EventCategory.CONCERT, "București", "90.00",
                LocalDateTime.of(2026, 12, 20, 21, 0));
        theatreIasi = event(EventCategory.THEATRE, "Iași", "100.00",
                LocalDateTime.of(2027, 1, 5, 18, 0));

        // already sorted by date, as returned by EventCatalogService.getUpcomingEvents
        allEvents = List.of(standupBucharestCheap, standupBucharestPricey, standupCluj,
                concertBucharest, theatreIasi);
    }

    // AC1: filters can be combined (stand-up in București under 100 RON)
    @Test
    void combinesCategoryCityAndMaxPrice() {
        EventFilter filter = new EventFilter(EventCategory.STANDUP, "București",
                null, null, null, new BigDecimal("100"));

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(standupBucharestCheap), result);
    }

    // AC1: a single filter (category) works on its own
    @Test
    void filtersByCategoryOnly() {
        EventFilter filter = new EventFilter(EventCategory.STANDUP, null, null, null, null, null);

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(standupBucharestCheap, standupBucharestPricey, standupCluj), result);
    }

    // AC1: city filter ignores letter case and surrounding spaces
    @Test
    void cityFilterIgnoresCaseAndSpaces() {
        EventFilter filter = new EventFilter(null, "  bucurești ", null, null, null, null);

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(standupBucharestCheap, standupBucharestPricey, concertBucharest), result);
    }

    // AC1: combining category, city, date interval and price interval at once
    @Test
    void combinesAllFilters() {
        EventFilter filter = new EventFilter(EventCategory.STANDUP, "București",
                LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 31),
                new BigDecimal("100"), new BigDecimal("200"));

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(standupBucharestPricey), result);
    }

    // AC2: date interval includes events on both the start and the end day
    @Test
    void dateIntervalIncludesBothEnds() {
        EventFilter filter = new EventFilter(null, null,
                LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 20), null, null);

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(standupBucharestPricey, standupCluj, concertBucharest), result);
    }

    // AC2: only a start date -> every event from that day on
    @Test
    void dateFromOnlyReturnsEventsFromThatDayOn() {
        EventFilter filter = new EventFilter(null, null,
                LocalDate.of(2026, 12, 20), null, null, null);

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(concertBucharest, theatreIasi), result);
    }

    // AC2: only an end date -> every event up to that day
    @Test
    void dateToOnlyReturnsEventsUntilThatDay() {
        EventFilter filter = new EventFilter(null, null,
                null, LocalDate.of(2026, 12, 12), null, null);

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(standupBucharestCheap, standupBucharestPricey), result);
    }

    // AC2: a start date after the end date is an invalid interval
    @Test
    void dateFromAfterDateToThrowsException() {
        EventFilter filter = new EventFilter(null, null,
                LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 10), null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.filter(allEvents, filter));
        assertEquals("Start date cannot be after end date", ex.getMessage());
    }

    // AC3: price interval includes the minimum and the maximum
    @Test
    void priceIntervalIncludesMinAndMax() {
        EventFilter filter = new EventFilter(null, null, null, null,
                new BigDecimal("80"), new BigDecimal("100"));

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(standupBucharestCheap, concertBucharest, theatreIasi), result);
    }

    // AC3: only a minimum price
    @Test
    void minPriceOnly() {
        EventFilter filter = new EventFilter(null, null, null, null, new BigDecimal("100"), null);

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(standupBucharestPricey, theatreIasi), result);
    }

    // AC3: a minimum price greater than the maximum is an invalid interval
    @Test
    void minPriceGreaterThanMaxPriceThrowsException() {
        EventFilter filter = new EventFilter(null, null, null, null,
                new BigDecimal("200"), new BigDecimal("100"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.filter(allEvents, filter));
        assertEquals("Minimum price cannot be greater than maximum price", ex.getMessage());
    }

    // AC4: no matching events -> empty list (the page then shows "No events found")
    @Test
    void noMatchingEventsReturnsEmptyList() {
        EventFilter filter = new EventFilter(EventCategory.THEATRE, "Cluj-Napoca",
                null, null, null, null);

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(List.of(), result);
    }

    // AC5: after reset (no filters set) all events are shown, in the same order
    @Test
    void noFiltersReturnsAllEvents() {
        EventFilter filter = new EventFilter(null, null, null, null, null, null);

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(allEvents, result);
    }

    // AC5: an empty city field (as sent by the reset form) is ignored
    @Test
    void blankCityIsIgnored() {
        EventFilter filter = new EventFilter(null, "   ", null, null, null, null);

        List<Event> result = service.filter(allEvents, filter);

        assertEquals(allEvents, result);
    }

    // --- helpers ---

    private Event event(EventCategory category, String city, String price, LocalDateTime dateTime) {
        return new Event("Test " + category, category, city, "Venue",
                dateTime, new BigDecimal(price), "Description");
    }
}
