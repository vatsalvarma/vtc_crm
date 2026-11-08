package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_counter_bills")
public class CounterBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String billNumber;

    @Column(length = 100)
    private String customerName;

    @Column(length = 20)
    private String customerPhone;

    @Column
    private Long clientId;

    @Column(length = 30)
    private String clientType; // "walk_in", "dealer", "cafe"

    @Column(length = 30)
    private String billType = "counter"; // "counter", "dealer", "cafe"

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @Column(nullable = false)
    private Long totalPaise = 0L;

    @Column
    private Long subtotalPaise = 0L;

    @Column
    private Long discountPaise = 0L;

    @Column(length = 120)
    private String discountReason;

    @Column
    private Long paidPaise = 0L;

    @Column
    private Long duePaise = 0L;

    @Column(length = 200)
    private String creditReason;

    @Column(nullable = false, length = 20)
    private String paymentMode = "CASH"; // "CASH", "UPI", "CARD", "SPLIT", "CREDIT"

    @Column(length = 50)
    private String cashierName;

    @Column(nullable = false, length = 30)
    private String status = "PAID"; // "PAID", "CANCEL_REQUESTED", "CANCELLED", "REFUNDED"

    @Column(nullable = false)
    private Integer reprints = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CounterBill() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }

    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }

    public String getItemsJson() { return itemsJson; }
    public void setItemsJson(String itemsJson) { this.itemsJson = itemsJson; }

    public Long getTotalPaise() { return totalPaise; }
    public void setTotalPaise(Long totalPaise) { this.totalPaise = totalPaise; }

    public Long getSubtotalPaise() { return subtotalPaise; }
    public void setSubtotalPaise(Long subtotalPaise) { this.subtotalPaise = subtotalPaise; }

    public Long getDiscountPaise() { return discountPaise; }
    public void setDiscountPaise(Long discountPaise) { this.discountPaise = discountPaise; }

    public String getDiscountReason() { return discountReason; }
    public void setDiscountReason(String discountReason) { this.discountReason = discountReason; }

    public Long getPaidPaise() { return paidPaise; }
    public void setPaidPaise(Long paidPaise) { this.paidPaise = paidPaise; }

    public Long getDuePaise() { return duePaise; }
    public void setDuePaise(Long duePaise) { this.duePaise = duePaise; }

    public String getCreditReason() { return creditReason; }
    public void setCreditReason(String creditReason) { this.creditReason = creditReason; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getReprints() { return reprints; }
    public void setReprints(Integer reprints) { this.reprints = reprints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
