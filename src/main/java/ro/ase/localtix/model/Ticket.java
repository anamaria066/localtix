package ro.ase.localtix.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * An electronic ticket (US-12), generated for each paid seat.
 * The event, date and location come from the seat (seat.getEvent()).
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique code, e.g. LTX-2026-A1B2C3. */
    @Column(unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    private Long userId;
    private String customerName;

    /** The final price paid for this ticket - needed for the refund (US-10). */
    @Column(precision = 10, scale = 2)
    private BigDecimal paidPrice;

    /** The amount refunded when the ticket was cancelled (null while the ticket is active). */
    @Column(precision = 10, scale = 2)
    private BigDecimal refundedAmount;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.ACTIVE;

    private LocalDateTime createdAt;

    public Ticket() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Seat getSeat() { return seat; }
    public void setSeat(Seat seat) { this.seat = seat; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getPaidPrice() { return paidPrice; }
    public void setPaidPrice(BigDecimal paidPrice) { this.paidPrice = paidPrice; }

    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
