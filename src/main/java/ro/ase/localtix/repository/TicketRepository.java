package ro.ase.localtix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.ase.localtix.model.Ticket;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** US-12 "My Tickets": the tickets of a customer. */
    List<Ticket> findByUserId(Long userId);

    /** US-12: the codes already used, so a new ticket never gets an existing code. */
    @Query("select t.code from Ticket t")
    List<String> findAllCodes();
}
