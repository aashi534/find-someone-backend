package com.findsomeone.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "connection_messages", indexes = @Index(
        name = "idx_connection_messages_connection_created",
        columnList = "connection_id,created_at"))
public class ConnectionMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false)
    private Connection connection;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 5000)
    private String body;

    // Nullable so messages created before purpose-labelled chats can be retained.
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ConnectionPurpose purpose;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void beforeInsert() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Connection getConnection() { return connection; }
    public void setConnection(Connection connection) { this.connection = connection; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public ConnectionPurpose getPurpose() { return purpose; }
    public void setPurpose(ConnectionPurpose purpose) { this.purpose = purpose; }
    public Instant getCreatedAt() { return createdAt; }
}
