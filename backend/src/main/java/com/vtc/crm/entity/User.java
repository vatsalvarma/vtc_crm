package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 30)
    private String role; // "Admin", "Manager", "Staff"

    @Column(nullable = false, length = 20)
    private String loginType; // "password" or "pin"

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 255)
    private String pinHash;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // "ACTIVE", "LOCKED", "SUSPENDED"

    @Column(length = 50)
    private String lastSignIn;

    @Column(nullable = false)
    private Boolean mustChangeSecret = false;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 10)
    private String avatarInitials;

    @Column
    private Long reportsToId;

    @Column(length = 50)
    private String reportsToName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public User() {}

    public User(String username, String fullName, String role, String loginType) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.loginType = loginType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLoginType() { return loginType; }
    public void setLoginType(String loginType) { this.loginType = loginType; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLastSignIn() { return lastSignIn; }
    public void setLastSignIn(String lastSignIn) { this.lastSignIn = lastSignIn; }

    public Boolean getMustChangeSecret() { return mustChangeSecret; }
    public void setMustChangeSecret(Boolean mustChangeSecret) { this.mustChangeSecret = mustChangeSecret; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarInitials() { return avatarInitials; }
    public void setAvatarInitials(String avatarInitials) { this.avatarInitials = avatarInitials; }

    public Long getReportsToId() { return reportsToId; }
    public void setReportsToId(Long reportsToId) { this.reportsToId = reportsToId; }

    public String getReportsToName() { return reportsToName; }
    public void setReportsToName(String reportsToName) { this.reportsToName = reportsToName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

