package com.findsomeone.dto;

import com.findsomeone.entity.SafetyReportReason;
import com.findsomeone.entity.SafetyReportStatus;
import java.time.Instant;

public record SafetyReportResponse(
        Long id,
        Long reportedUserId,
        Long reportedMessageId,
        SafetyReportReason reason,
        SafetyReportStatus status,
        Instant createdAt) {
}
