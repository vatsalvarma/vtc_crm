package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_order_dispatches")
public class OrderDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Double kg = 0.0;

    @Column(nullable = false, length = 30)
    private String mode = "RAPIDO"; // "RAPIDO", "UBER", "PORTER", "DUNZO", "SELF", "CUSTOMER", "TRANSPORT"

    @Column(length = 100)
    private String bookingRef;

    @Column(length = 100)
    private String handoverTo;

    @Column(length = 255)
    private String evidence;

    @Column
    private Double deliveredKg;

    @Column(length = 255)
    private String podNote;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime deliveredAt;

    public OrderDispatch() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Double getKg() { return kg; }
    public void setKg(Double kg) { this.kg = kg; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }

    public String getHandoverTo() { return handoverTo; }
    public void setHandoverTo(String handoverTo) { this.handoverTo = handoverTo; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public Double getDeliveredKg() { return deliveredKg; }
    public void setDeliveredKg(Double deliveredKg) { this.deliveredKg = deliveredKg; }

    public String getPodNote() { return podNote; }
    public void setPodNote(String podNote) { this.podNote = podNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}

