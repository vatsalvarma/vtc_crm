package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_roast_batches")
public class RoastBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String batchCode;

    @Column(nullable = false)
    private Double greenKg;

    @Column(nullable = false)
    private Double roastedKg;

    @Column(nullable = false)
    private Double lossPercent;

    @Column(length = 100)
    private String profileName;

    @Column(length = 50)
    private String roasterName;

    @Column(nullable = false, length = 20)
    private String qcStatus = "HOLD"; // "HOLD", "APPROVED", "REJECTED"

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public RoastBatch() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public Double getGreenKg() { return greenKg; }
    public void setGreenKg(Double greenKg) { this.greenKg = greenKg; }

    public Double getRoastedKg() { return roastedKg; }
    public void setRoastedKg(Double roastedKg) { this.roastedKg = roastedKg; }

    public Double getLossPercent() { return lossPercent; }
    public void setLossPercent(Double lossPercent) { this.lossPercent = lossPercent; }

    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }

    public String getRoasterName() { return roasterName; }
    public void setRoasterName(String roasterName) { this.roasterName = roasterName; }

    public String getQcStatus() { return qcStatus; }
    public void setQcStatus(String qcStatus) { this.qcStatus = qcStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

