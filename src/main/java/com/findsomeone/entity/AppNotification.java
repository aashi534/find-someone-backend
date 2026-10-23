package com.findsomeone.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_notifications", indexes = {
        @Index(name = "idx_notifications_recipient_created",
                columnList = "recipient_id,created_at"),
        @Index(name = "idx_notifications_recipient_read",
                columnList = "recipient_id,read_at")
})
public class AppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 48)
    private NotificationType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "message_text", nullable = false, length = 500)
    private String message;

    @Column(length = 32)
    private String referenceType;

    private Long referenceId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant readAt;

    @PrePersist
    void beforeInsert() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
}
