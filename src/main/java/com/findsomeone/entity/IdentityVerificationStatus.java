package com.findsomeone.entity;

/** Set only by a trusted identity-verification integration, never by profile APIs. */
public enum IdentityVerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    REJECTED
}
