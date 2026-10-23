package com.findsomeone.dto;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long senderId,
        String senderName,
        String body,
        Instant createdAt) {
}
