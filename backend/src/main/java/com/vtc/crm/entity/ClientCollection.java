package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_collections")
public class ClientCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false, length = 20)
    private String day;

    @Column(nullable = false)
    private Long amountPaise;

    @Column(nullable = false, length = 20)
    private String method; // "CASH", "UPI", "BANK", "CHEQUE"

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String note;

    @Column(length = 100)
    private String createdByName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime cancelledAt;

    @Column(length = 255)
    private String cancelReason;

    public ClientCollection() {}

    public ClientCollection(Long clientId, String day, Long amountPaise, String method, String reference, String note, String createdByName) {
        this.clientId = clientId;
        this.day = day;
        this.amountPaise = amountPaise;
        this.method = method;
        this.reference = reference;
        this.note = note;
        this.createdByName = createdByName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
}

