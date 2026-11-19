package ro.ase.localtix.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * An order of the customer (US-11): the seats from the cart, the total and the status.
 * A new order is PENDING; after a successful payment it becomes PAID.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToMany
    @JoinTable(name = "order_seats")
    private List<Seat> seats = new ArrayList<>();

    private BigDecimal total;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    public Order() {}

    public Order(Long userId, List<Seat> seats, BigDecimal total, LocalDateTime createdAt) {
        this.userId = userId;
        this.seats = new ArrayList<>(seats);
        this.total = total;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
