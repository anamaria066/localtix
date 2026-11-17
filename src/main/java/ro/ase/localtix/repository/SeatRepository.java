package ro.ase.localtix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.localtix.model.Seat;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventId(Long eventId);
}