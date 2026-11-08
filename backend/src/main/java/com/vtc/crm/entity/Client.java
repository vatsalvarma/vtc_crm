package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String contact;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String gstin;

    @Column(nullable = false, length = 20)
    private String clientType = "dealer"; // "dealer", "cafe", "unsorted"

    @Column(nullable = false, length = 10)
    private String stateCode = "36";

    @Column
    private Long creditLimitPaise;

    @Column(nullable = false)
    private Long openingPaise = 0L;

    @Column(length = 20)
    private String openingDate;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Client() {}

    public Client(String name, String contact, String phone, String address, String gstin, String clientType, String stateCode, Long openingPaise, String openingDate) {
        this.name = name;
        this.contact = contact;
        this.phone = phone;
        this.address = address;
        this.gstin = gstin;
        this.clientType = clientType;
        this.stateCode = stateCode;
        this.openingPaise = openingPaise != null ? openingPaise : 0L;
        this.openingDate = openingDate;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }

    public String getStateCode() { return stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = stateCode; }

    public Long getCreditLimitPaise() { return creditLimitPaise; }
    public void setCreditLimitPaise(Long creditLimitPaise) { this.creditLimitPaise = creditLimitPaise; }

    public Long getOpeningPaise() { return openingPaise; }
    public void setOpeningPaise(Long openingPaise) { this.openingPaise = openingPaise; }

    public String getOpeningDate() { return openingDate; }
    public void setOpeningDate(String openingDate) { this.openingDate = openingDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

