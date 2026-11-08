package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_masters")
public class MasterRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String kind; // "CUSTOMER", "SUPPLIER", "PRODUCT"

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 30)
    private String gst;

    @Column(nullable = false, length = 20)
    private String state = "ACTIVE"; // "ACTIVE", "DRAFT", "ARCHIVED"

    @Column(columnDefinition = "TEXT")
    private String metaJson;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MasterRecord() {}

    public MasterRecord(String kind, String code, String name, String state) {
        this.kind = kind;
        this.code = code;
        this.name = name;
        this.state = state;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGst() { return gst; }
    public void setGst(String gst) { this.gst = gst; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getMetaJson() { return metaJson; }
    public void setMetaJson(String metaJson) { this.metaJson = metaJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

