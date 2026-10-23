package com.findsomeone.dto;

import com.findsomeone.entity.ConnectionPurpose;
import java.time.Instant;
import java.util.Set;

public record ConnectionResponse(
        Long id,
        Long otherUserId,
        String otherUserName,
        Set<ConnectionPurpose> purposes,
        Instant connectedAt) {
}
