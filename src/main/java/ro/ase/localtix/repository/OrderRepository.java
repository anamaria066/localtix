package ro.ase.localtix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.localtix.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
