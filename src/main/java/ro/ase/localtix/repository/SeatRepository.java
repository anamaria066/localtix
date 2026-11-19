package ro.ase.localtix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.localtix.model.Seat;
import ro.ase.localtix.model.SeatStatus;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventId(Long eventId);

    /** US-9: the seats locked by a customer (at all events) - the candidates for the cart. */
    List<Seat> findByStatusAndLockedByUserId(SeatStatus status, Long lockedByUserId);
}
