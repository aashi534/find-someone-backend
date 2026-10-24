package com.findsomeone.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String bio;
    @Column(length = 100)
    private String city;

    @JsonIgnore
    @Column(name = "linkedin_subject", unique = true)
    private String linkedinSubject;

    @Column(name = "email_verified", nullable = false,
            columnDefinition = "boolean default false")
    private boolean emailVerified;

    @Column(name = "discoverable", nullable = false,
            columnDefinition = "boolean default true")
    private boolean discoverable = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_connection_purposes",
            joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private Set<ConnectionPurpose> connectionPurposes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_verification_status", nullable = false,
            columnDefinition = "varchar(32) default 'UNVERIFIED'")
    private IdentityVerificationStatus identityVerificationStatus =
            IdentityVerificationStatus.UNVERIFIED;

    @Column(name = "identity_verified_at")
    private Instant identityVerifiedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @JsonIgnore
    public String getLinkedinSubject() {
        return linkedinSubject;
    }

    public void setLinkedinSubject(String linkedinSubject) {
        this.linkedinSubject = linkedinSubject;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isDiscoverable() {
        return discoverable;
    }

    public void setDiscoverable(boolean discoverable) {
        this.discoverable = discoverable;
    }

    public Set<ConnectionPurpose> getConnectionPurposes() {
        return connectionPurposes;
    }

    public void setConnectionPurposes(Set<ConnectionPurpose> connectionPurposes) {
        this.connectionPurposes = connectionPurposes == null
                ? new HashSet<>() : new HashSet<>(connectionPurposes);
    }

    public IdentityVerificationStatus getIdentityVerificationStatus() {
        return identityVerificationStatus;
    }

    @JsonIgnore
    public Instant getIdentityVerifiedAt() {
        return identityVerifiedAt;
    }

    /** Internal hook for a verified identity provider; not exposed by profile endpoints. */
    public void markIdentityVerified(Instant verifiedAt) {
        this.identityVerificationStatus = IdentityVerificationStatus.VERIFIED;
        this.identityVerifiedAt = verifiedAt;
    }
}
