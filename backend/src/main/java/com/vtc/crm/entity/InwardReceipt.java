package com.vtc.crm.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_inward_receipts")
public class InwardReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 50)
    private String source; // "AUCTION", "OTHER"

    @Column(name = "source_name", length = 150)
    @JsonProperty("source_name")
    private String sourceName;

    @Column(name = "product_id")
    @JsonProperty("product_id")
    private Long productId;

    @Column(length = 150)
    private String product;

    @Column(nullable = false)
    private Integer bags = 0;

    @Column(nullable = false)
    private Integer damaged = 0;

    @Column(nullable = false)
    private Integer partial = 0;

    @Column(name = "partial_kg")
    @JsonProperty("partial_kg")
    private Double partialKg;

    @Column(name = "nominal_kg")
    @JsonProperty("nominal_kg")
    private Double nominalKg = 0.0;

    @Column(name = "allocatable_kg")
    @JsonProperty("allocatable_kg")
    private Double allocatableKg = 0.0;

    @Column(name = "quarantine_kg")
    @JsonProperty("quarantine_kg")
    private Double quarantineKg = 0.0;

    @Column(name = "invoice_status", length = 30)
    @JsonProperty("invoice_status")
    private String invoiceStatus = "MATCHED";

    @Column(name = "payment_status", length = 30)
    @JsonProperty("payment_status")
    private String paymentStatus = "PENDING";

    @Column(name = "created_by", length = 100)
    @JsonProperty("created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    @JsonProperty("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reversed_at")
    @JsonProperty("reversed_at")
    private LocalDateTime reversedAt;

    @Column(name = "reverse_reason", length = 255)
    @JsonProperty("reverse_reason")
    private String reverseReason;

    public InwardReceipt() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public Integer getBags() { return bags; }
    public void setBags(Integer bags) { this.bags = bags; }

    public Integer getDamaged() { return damaged; }
    public void setDamaged(Integer damaged) { this.damaged = damaged; }

    public Integer getPartial() { return partial; }
    public void setPartial(Integer partial) { this.partial = partial; }

    public Double getPartialKg() { return partialKg; }
    public void setPartialKg(Double partialKg) { this.partialKg = partialKg; }

    public Double getNominalKg() { return nominalKg; }
    public void setNominalKg(Double nominalKg) { this.nominalKg = nominalKg; }

    public Double getAllocatableKg() { return allocatableKg; }
    public void setAllocatableKg(Double allocatableKg) { this.allocatableKg = allocatableKg; }

    public Double getQuarantineKg() { return quarantineKg; }
    public void setQuarantineKg(Double quarantineKg) { this.quarantineKg = quarantineKg; }

    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReversedAt() { return reversedAt; }
    public void setReversedAt(LocalDateTime reversedAt) { this.reversedAt = reversedAt; }

    public String getReverseReason() { return reverseReason; }
    public void setReverseReason(String reverseReason) { this.reverseReason = reverseReason; }
}
