package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_outgoing_orders")
public class OutgoingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId = 1L;

    @Column(nullable = false, length = 150)
    private String customer = "Sample Customer";

    @Column(length = 20)
    private String customerPhone;

    @Column(length = 255)
    private String customerAddress;

    @Column(nullable = false, length = 30)
    private String form = "ROASTED"; // "ROASTED", "RAW", "GROUND", "BLENDED"

    @Column
    private Long productId;

    @Column(nullable = false, length = 150)
    private String item;

    @Column(nullable = false)
    private Double quantityKg = 0.0;

    @Column(nullable = false)
    private Double reservedKg = 0.0;

    @Column(nullable = false)
    private Double dispatchedKg = 0.0;

    @Column
    private Long pricePerKgPaise;

    @Column
    private Integer gstRateBp;

    @Column(nullable = false)
    private Boolean creditRequested = false;

    @Column(length = 150)
    private String creditNote;

    @Column(nullable = false)
    private Boolean cleared = true;

    @Column(length = 100)
    private String clearanceNote = "Cleared";

    @Column(nullable = false, length = 30)
    private String state = "RESERVED"; // "DRAFT", "CONFIRMED", "RESERVED", "DISPATCHED", "DELIVERED", "CANCELLED"

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 100)
    private String createdByName;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public OutgoingOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public Double getQuantityKg() { return quantityKg; }
    public void setQuantityKg(Double quantityKg) { this.quantityKg = quantityKg; }

    public Double getReservedKg() { return reservedKg; }
    public void setReservedKg(Double reservedKg) { this.reservedKg = reservedKg; }

    public Double getDispatchedKg() { return dispatchedKg; }
    public void setDispatchedKg(Double dispatchedKg) { this.dispatchedKg = dispatchedKg; }

    public Long getPricePerKgPaise() { return pricePerKgPaise; }
    public void setPricePerKgPaise(Long pricePerKgPaise) { this.pricePerKgPaise = pricePerKgPaise; }

    public Integer getGstRateBp() { return gstRateBp; }
    public void setGstRateBp(Integer gstRateBp) { this.gstRateBp = gstRateBp; }

    public Boolean getCreditRequested() { return creditRequested; }
    public void setCreditRequested(Boolean creditRequested) { this.creditRequested = creditRequested; }

    public String getCreditNote() { return creditNote; }
    public void setCreditNote(String creditNote) { this.creditNote = creditNote; }

    public Boolean getCleared() { return cleared; }
    public void setCleared(Boolean cleared) { this.cleared = cleared; }

    public String getClearanceNote() { return clearanceNote; }
    public void setClearanceNote(String clearanceNote) { this.clearanceNote = clearanceNote; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

