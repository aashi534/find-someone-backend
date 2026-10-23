package com.findsomeone.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "connection_requests", indexes = {
        @Index(name = "idx_connection_requests_recipient_status",
                columnList = "recipient_id,status"),
        @Index(name = "idx_connection_requests_requester_status",
                columnList = "requester_id,status")
})
public class ConnectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionPurpose purpose;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionRequestStatus status = ConnectionRequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public ConnectionPurpose getPurpose() { return purpose; }
    public void setPurpose(ConnectionPurpose purpose) { this.purpose = purpose; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public ConnectionRequestStatus getStatus() { return status; }
    public void setStatus(ConnectionRequestStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
