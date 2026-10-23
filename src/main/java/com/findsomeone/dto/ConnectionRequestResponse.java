package com.findsomeone.dto;

import com.findsomeone.entity.ConnectionPurpose;
import com.findsomeone.entity.ConnectionRequestStatus;
import java.time.Instant;

public record ConnectionRequestResponse(
        Long id,
        Long requesterId,
        String requesterName,
        Long recipientId,
        String recipientName,
        ConnectionPurpose purpose,
        String note,
        ConnectionRequestStatus status,
        Instant createdAt) {
}
