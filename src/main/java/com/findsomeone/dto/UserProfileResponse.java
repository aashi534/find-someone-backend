package com.findsomeone.dto;

import com.findsomeone.entity.ConnectionPurpose;
import com.findsomeone.entity.IdentityVerificationStatus;
import java.util.Set;

public record UserProfileResponse(
        Long id,
        String name,
        String bio,
        Set<ConnectionPurpose> connectionPurposes,
        IdentityVerificationStatus identityVerificationStatus,
        boolean emailVerified) {
}
