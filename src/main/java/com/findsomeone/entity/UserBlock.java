package com.findsomeone.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_blocks",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_block_pair",
                columnNames = {"blocker_id", "blocked_id"}),
        indexes = @Index(name = "idx_user_blocks_blocked", columnList = "blocked_id"))
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void beforeInsert() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getBlocker() { return blocker; }
    public void setBlocker(User blocker) { this.blocker = blocker; }
    public User getBlocked() { return blocked; }
    public void setBlocked(User blocked) { this.blocked = blocked; }
    public Instant getCreatedAt() { return createdAt; }
}
