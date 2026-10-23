package com.findsomeone.dto;

import com.findsomeone.entity.NotificationType;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        Long actorId,
        String actorName,
        String referenceType,
        Long referenceId,
        Instant createdAt,
        Instant readAt) {
}
