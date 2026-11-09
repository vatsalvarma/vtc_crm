package com.vtc.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vtc_tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String kind; // ROAST, PACK, QC, OUTSIDE, COUNTER, DISPATCH, INWARD, BLEND, COUNT, GENERAL

    @Column(length = 50)
    private String kindLabel;

    @Column(length = 30)
    private String unit;

    @Column(nullable = false, length = 30)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, BLOCKED, SUBMITTED, DONE

    @Column
    private Double progress = 0.0;

    @Column
    private Double target;

    @Column(length = 30)
    private String due;

    @Column
    private Long assignedTo;

    @Column(length = 100)
    private String assignee;

    @Column(length = 100)
    private String assignedBy;

    @Column
    private Long assignedById;

    @Column(length = 500)
    private String submitNote;

    @Column(length = 500)
    private String reviewNote;

    @Column
    private Long reviewedById;

    @Column(length = 100)
    private String reviewedByName;

    @Column
    private Long refId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Task() {}

    public Task(String title, String kind, String kindLabel, String unit, Double target, Double progress, String due,
                Long assignedTo, String assignee, Long assignedById, String assignedBy, String status) {
        this.title = title;
        this.kind = kind;
        this.kindLabel = kindLabel;
        this.unit = unit;
        this.target = target;
        this.progress = progress != null ? progress : 0.0;
        this.due = due;
        this.assignedTo = assignedTo;
        this.assignee = assignee;
        this.assignedById = assignedById;
        this.assignedBy = assignedBy;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getKindLabel() { return kindLabel; }
    public void setKindLabel(String kindLabel) { this.kindLabel = kindLabel; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getProgress() { return progress; }
    public void setProgress(Double progress) { this.progress = progress; }

    public Double getTarget() { return target; }
    public void setTarget(Double target) { this.target = target; }

    public String getDue() { return due; }
    public void setDue(String due) { this.due = due; }

    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }

    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }

    public Long getAssignedById() { return assignedById; }
    public void setAssignedById(Long assignedById) { this.assignedById = assignedById; }

    public String getSubmitNote() { return submitNote; }
    public void setSubmitNote(String submitNote) { this.submitNote = submitNote; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public Long getReviewedById() { return reviewedById; }
    public void setReviewedById(Long reviewedById) { this.reviewedById = reviewedById; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
