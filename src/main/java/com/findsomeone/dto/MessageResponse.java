package com.findsomeone.dto;

import com.findsomeone.entity.ConnectionPurpose;
import java.time.Instant;

public record MessageResponse(
        Long id,
        Long senderId,
        String senderName,
        String body,
        ConnectionPurpose purpose,
        Instant createdAt) {
}
