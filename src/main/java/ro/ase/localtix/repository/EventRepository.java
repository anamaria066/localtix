package ro.ase.localtix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.localtix.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}