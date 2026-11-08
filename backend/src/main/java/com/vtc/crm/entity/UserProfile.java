package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_user_profiles")
public class UserProfile {

    @Id
    private Long userId;

    @Column(length = 50)
    private String emergencyContact;

    @Column(length = 10)
    private String aadhaarLast4;

    @Column(length = 10)
    private String panLast4;

    @Column(length = 10)
    private String bankAccountLast4;

    @Column(length = 20)
    private String ifsc;

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String joiningDate;

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public UserProfile() {}

    public UserProfile(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getAadhaarLast4() { return aadhaarLast4; }
    public void setAadhaarLast4(String aadhaarLast4) { this.aadhaarLast4 = aadhaarLast4; }

    public String getPanLast4() { return panLast4; }
    public void setPanLast4(String panLast4) { this.panLast4 = panLast4; }

    public String getBankAccountLast4() { return bankAccountLast4; }
    public void setBankAccountLast4(String bankAccountLast4) { this.bankAccountLast4 = bankAccountLast4; }

    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getJoiningDate() { return joiningDate; }
    public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

