package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_packing_tickets")
public class PackingTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String productName;

    @Column(nullable = false, length = 30)
    private String demandType = "STOCK"; // "STOCK", "ORDER"

    @Column(nullable = false)
    private Integer packsTarget = 20;

    @Column(nullable = false)
    private Integer packsDone = 0;

    @Column(nullable = false)
    private Double packG = 500.0;

    @Column(length = 100)
    private String labelRef;

    @Column
    private Double inputKg;

    @Column(nullable = false)
    private Integer rejectedPacks = 0;

    @Column(nullable = false, length = 30)
    private String finishedStatus = "QC_HOLD"; // "QC_HOLD", "RELEASED", "REJECTED"

    @Column(nullable = false, length = 30)
    private String state = "OPEN"; // "OPEN", "IN_PROGRESS", "COMPLETE"

    @Column
    private Long sourceLotId;

    @Column
    private Long outputLotId;

    @Column(length = 50)
    private String outputLotCode;

    @Column(length = 100)
    private String createdByName;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PackingTicket() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDemandType() { return demandType; }
    public void setDemandType(String demandType) { this.demandType = demandType; }

    public Integer getPacksTarget() { return packsTarget; }
    public void setPacksTarget(Integer packsTarget) { this.packsTarget = packsTarget; }

    public Integer getPacksDone() { return packsDone; }
    public void setPacksDone(Integer packsDone) { this.packsDone = packsDone; }

    public Double getPackG() { return packG; }
    public void setPackG(Double packG) { this.packG = packG; }

    public String getLabelRef() { return labelRef; }
    public void setLabelRef(String labelRef) { this.labelRef = labelRef; }

    public Double getInputKg() { return inputKg; }
    public void setInputKg(Double inputKg) { this.inputKg = inputKg; }

    public Integer getRejectedPacks() { return rejectedPacks; }
    public void setRejectedPacks(Integer rejectedPacks) { this.rejectedPacks = rejectedPacks; }

    public String getFinishedStatus() { return finishedStatus; }
    public void setFinishedStatus(String finishedStatus) { this.finishedStatus = finishedStatus; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Long getSourceLotId() { return sourceLotId; }
    public void setSourceLotId(Long sourceLotId) { this.sourceLotId = sourceLotId; }

    public Long getOutputLotId() { return outputLotId; }
    public void setOutputLotId(Long outputLotId) { this.outputLotId = outputLotId; }

    public String getOutputLotCode() { return outputLotCode; }
    public void setOutputLotCode(String outputLotCode) { this.outputLotCode = outputLotCode; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

