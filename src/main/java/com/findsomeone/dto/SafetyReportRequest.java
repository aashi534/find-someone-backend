package com.findsomeone.dto;

import com.findsomeone.entity.SafetyReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SafetyReportRequest(
        @NotNull Long reportedUserId,
        @NotNull SafetyReportReason reason,
        @Size(max = 2000) String details) {
}
