package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 30)
    private String kind = "group"; // announcement, group, direct, record

    @Column(length = 50)
    private String recordType;

    @Column
    private Long recordId;

    @Column
    private Long directUser1Id;

    @Column
    private Long directUser2Id;

    @Column(length = 500)
    private String membersCsv; // e.g. "1,2,3,4,5" or "*" for announcement

    @Column(length = 1000)
    private String lastMessageBody;

    @Column(length = 100)
    private String lastMessageAuthor;

    @Column
    private LocalDateTime lastMessageAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Conversation() {}

    public Conversation(String title, String kind, String membersCsv) {
        this.title = title;
        this.kind = kind;
        this.membersCsv = membersCsv;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public Long getDirectUser1Id() { return directUser1Id; }
    public void setDirectUser1Id(Long directUser1Id) { this.directUser1Id = directUser1Id; }

    public Long getDirectUser2Id() { return directUser2Id; }
    public void setDirectUser2Id(Long directUser2Id) { this.directUser2Id = directUser2Id; }

    public String getMembersCsv() { return membersCsv; }
    public void setMembersCsv(String membersCsv) { this.membersCsv = membersCsv; }

    public String getLastMessageBody() { return lastMessageBody; }
    public void setLastMessageBody(String lastMessageBody) { this.lastMessageBody = lastMessageBody; }

    public String getLastMessageAuthor() { return lastMessageAuthor; }
    public void setLastMessageAuthor(String lastMessageAuthor) { this.lastMessageAuthor = lastMessageAuthor; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

