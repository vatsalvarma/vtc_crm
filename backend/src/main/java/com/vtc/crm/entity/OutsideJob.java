package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_outside_jobs")
public class OutsideJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 150)
    private String customer;

    @Column(length = 100)
    private String beanType;

    @Column(nullable = false)
    private Double receivedKg = 0.0;

    @Column(nullable = false)
    private Double rawLeftKg = 0.0;

    @Column(nullable = false)
    private Double roastedLeftKg = 0.0;

    @Column
    private Double inputKg;

    @Column
    private Double outputKg;

    @Column(nullable = false)
    private Double returnedKg = 0.0;

    @Column(length = 30)
    private String chargeBasis = "INPUT_KG"; // "INPUT_KG", "OUTPUT_KG", "BATCH"

    @Column
    private Long ratePaise = 0L;

    @Column
    private Long chargePaise;

    @Column(length = 20)
    private String paymentStatus = "PENDING"; // "PENDING", "PAID"

    @Column(nullable = false, length = 30)
    private String state = "RECEIVED"; // "RECEIVED", "ROASTED", "RETURNED"

    @Column
    private Long roastedLotId;

    @Column(columnDefinition = "TEXT")
    private String returnNote;

    @Column(length = 100)
    private String receivedByName;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public OutsideJob() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getBeanType() { return beanType; }
    public void setBeanType(String beanType) { this.beanType = beanType; }

    public Double getReceivedKg() { return receivedKg; }
    public void setReceivedKg(Double receivedKg) { this.receivedKg = receivedKg; }

    public Double getRawLeftKg() { return rawLeftKg; }
    public void setRawLeftKg(Double rawLeftKg) { this.rawLeftKg = rawLeftKg; }

    public Double getRoastedLeftKg() { return roastedLeftKg; }
    public void setRoastedLeftKg(Double roastedLeftKg) { this.roastedLeftKg = roastedLeftKg; }

    public Double getInputKg() { return inputKg; }
    public void setInputKg(Double inputKg) { this.inputKg = inputKg; }

    public Double getOutputKg() { return outputKg; }
    public void setOutputKg(Double outputKg) { this.outputKg = outputKg; }

    public Double getReturnedKg() { return returnedKg; }
    public void setReturnedKg(Double returnedKg) { this.returnedKg = returnedKg; }

    public String getChargeBasis() { return chargeBasis; }
    public void setChargeBasis(String chargeBasis) { this.chargeBasis = chargeBasis; }

    public Long getRatePaise() { return ratePaise; }
    public void setRatePaise(Long ratePaise) { this.ratePaise = ratePaise; }

    public Long getChargePaise() { return chargePaise; }
    public void setChargePaise(Long chargePaise) { this.chargePaise = chargePaise; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Long getRoastedLotId() { return roastedLotId; }
    public void setRoastedLotId(Long roastedLotId) { this.roastedLotId = roastedLotId; }

    public String getReturnNote() { return returnNote; }
    public void setReturnNote(String returnNote) { this.returnNote = returnNote; }

    public String getReceivedByName() { return receivedByName; }
    public void setReceivedByName(String receivedByName) { this.receivedByName = receivedByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

