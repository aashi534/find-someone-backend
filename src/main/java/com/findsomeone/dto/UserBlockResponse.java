package com.findsomeone.dto;

import java.time.Instant;

public record UserBlockResponse(
        Long userId,
        String name,
        Instant createdAt) {
}
