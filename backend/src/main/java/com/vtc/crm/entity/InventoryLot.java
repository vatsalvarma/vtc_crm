package com.vtc.crm.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_inventory_lots")
public class InventoryLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "receipt_id")
    @JsonProperty("receipt_id")
    private Long receiptId;

    @Column(name = "receipt_code", length = 50)
    @JsonProperty("receipt_code")
    private String receiptCode;

    @Column(name = "product_id")
    @JsonProperty("product_id")
    private Long productId;

    @Column(length = 150)
    private String product;

    @Column(length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String stage = "RAW"; // "RAW", "ROASTED", "GROUND", "PACKED"

    @Column(nullable = false, length = 50)
    private String location = "PROD-01"; // "PROD-01", "CTR-01", "IN_TRANSIT"

    @Column(nullable = false, length = 50)
    private String owner = "VTC";

    @Column(nullable = false, length = 30)
    private String status = "RELEASED"; // "RELEASED", "QUARANTINE", "QC_HOLD", "REJECTED"

    @Column(name = "bag_kind", length = 30)
    @JsonProperty("bag_kind")
    private String bagKind = "SOUND"; // "SOUND", "DAMAGED", "PARTIAL"

    @Column
    private Integer bags = 0;

    @Column(nullable = false)
    private Double kg = 0.0;

    @Column(name = "available_kg", nullable = false)
    @JsonProperty("available_kg")
    private Double availableKg = 0.0;

    @Column(name = "reserved_kg", nullable = false)
    @JsonProperty("reserved_kg")
    private Double reservedKg = 0.0;

    @Column(name = "nominal_kg")
    @JsonProperty("nominal_kg")
    private Double nominalKg = 60.0;

    @Column
    private Boolean resting = false;

    @Column(name = "ready_from")
    @JsonProperty("ready_from")
    private LocalDate readyFrom;

    @Column(name = "created_at", nullable = false)
    @JsonProperty("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public InventoryLot() {}

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getReceiptId() { return receiptId; }
    public void setReceiptId(Long receiptId) { this.receiptId = receiptId; }

    public String getReceiptCode() { return receiptCode; }
    public void setReceiptCode(String receiptCode) { this.receiptCode = receiptCode; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBagKind() { return bagKind; }
    public void setBagKind(String bagKind) { this.bagKind = bagKind; }

    public Integer getBags() { return bags; }
    public void setBags(Integer bags) { this.bags = bags; }

    public Double getKg() { return kg; }
    public void setKg(Double kg) { this.kg = kg; }

    public Double getAvailableKg() { return availableKg; }
    public void setAvailableKg(Double availableKg) { this.availableKg = availableKg; }

    public Double getReservedKg() { return reservedKg; }
    public void setReservedKg(Double reservedKg) { this.reservedKg = reservedKg; }

    public Double getNominalKg() { return nominalKg; }
    public void setNominalKg(Double nominalKg) { this.nominalKg = nominalKg; }

    public Boolean getResting() { return resting; }
    public void setResting(Boolean resting) { this.resting = resting; }

    public LocalDate getReadyFrom() { return readyFrom; }
    public void setReadyFrom(LocalDate readyFrom) { this.readyFrom = readyFrom; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
