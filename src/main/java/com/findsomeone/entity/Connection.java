package com.findsomeone.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "connections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_connection_user_pair",
                columnNames = {"user_one_id", "user_two_id"}))
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stored in ascending user-id order so the unique constraint is symmetric.
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "connection_purposes",
            joinColumns = @JoinColumn(name = "connection_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private Set<ConnectionPurpose> purposes = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void beforeInsert() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUserOne() { return userOne; }
    public void setUserOne(User userOne) { this.userOne = userOne; }
    public User getUserTwo() { return userTwo; }
    public void setUserTwo(User userTwo) { this.userTwo = userTwo; }
    public Set<ConnectionPurpose> getPurposes() { return purposes; }
    public void addPurpose(ConnectionPurpose purpose) { purposes.add(purpose); }
    public Instant getCreatedAt() { return createdAt; }
}
