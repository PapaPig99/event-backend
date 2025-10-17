package com.example.eventproject.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
public class Registration {

    public enum RegStatus { PENDING, CONFIRMED, CANCELLED }
    public enum PayStatus { UNPAID, PAID }
    public enum CancelledReason { PAYMENT_TIMEOUT, USER_CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id")
    private EventSession session;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "zone_id")
    private EventZone zone; // nullable

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false)
    private RegStatus registrationStatus = RegStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PayStatus paymentStatus = PayStatus.UNPAID;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_reason")
    private CancelledReason cancelledReason;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // getters/setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public User getUser() { return user; }

    public void setUserId(Integer userId) { this.userId = userId; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public EventSession getSession() { return session; }
    public void setSession(EventSession session) { this.session = session; }
    public EventZone getZone() { return zone; }
    public void setZone(EventZone zone) { this.zone = zone; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public RegStatus getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(RegStatus registrationStatus) { this.registrationStatus = registrationStatus; }
    public PayStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PayStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getHoldExpiresAt() { return holdExpiresAt; }
    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) { this.holdExpiresAt = holdExpiresAt; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public CancelledReason getCancelledReason() { return cancelledReason; }
    public void setCancelledReason(CancelledReason cancelledReason) { this.cancelledReason = cancelledReason; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
