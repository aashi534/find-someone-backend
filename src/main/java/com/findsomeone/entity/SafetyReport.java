package com.findsomeone.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "safety_reports", indexes = {
        @Index(name = "idx_safety_reports_status_created", columnList = "status,created_at"),
        @Index(name = "idx_safety_reports_reported_user", columnList = "reported_user_id")
})
public class SafetyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SafetyReportReason reason;

    @Column(length = 2000)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SafetyReportStatus status = SafetyReportStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void beforeInsert() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }
    public User getReportedUser() { return reportedUser; }
    public void setReportedUser(User reportedUser) { this.reportedUser = reportedUser; }
    public SafetyReportReason getReason() { return reason; }
    public void setReason(SafetyReportReason reason) { this.reason = reason; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public SafetyReportStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
