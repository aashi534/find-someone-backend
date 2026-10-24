package com.findsomeone.dto;

import com.findsomeone.entity.ConnectionPurpose;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UpdateProfileRequest(
        @Size(max = 120) String name,
        @Size(max = 2000) String bio,
        @Size(max = 100) String city,
        Set<ConnectionPurpose> connectionPurposes,
        Boolean discoverable) {
}
