package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_user_documents")
public class UserDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String docType; // "Aadhaar", "PAN", "Employment Contract", "Bank Passbook"

    @Column(nullable = false, length = 100)
    private String fileName;

    @Column(length = 255)
    private String fileUrl;

    @Column(length = 50)
    private String uploadedAt;

    @Column(nullable = false, length = 20)
    private String status = "VERIFIED";

    public UserDocument() {}

    public UserDocument(Long userId, String docType, String fileName) {
        this.userId = userId;
        this.docType = docType;
        this.fileName = fileName;
        this.uploadedAt = LocalDateTime.now().toString();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

