package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversationId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(length = 50)
    private String authorRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(length = 50)
    private String audience = "all";

    @Column(length = 100)
    private String voiceFileId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Message() {}

    public Message(Long conversationId, Long authorId, String author, String authorRole, String body, String audience) {
        this.conversationId = conversationId;
        this.authorId = authorId;
        this.author = author;
        this.authorRole = authorRole;
        this.body = body;
        this.audience = audience != null ? audience : "all";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }

    public String getVoiceFileId() { return voiceFileId; }
    public void setVoiceFileId(String voiceFileId) { this.voiceFileId = voiceFileId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

