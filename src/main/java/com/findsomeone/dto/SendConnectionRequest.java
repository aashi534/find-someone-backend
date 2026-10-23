package com.findsomeone.dto;

import com.findsomeone.entity.ConnectionPurpose;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendConnectionRequest(
        @NotNull Long recipientId,
        @NotNull ConnectionPurpose purpose,
        @Size(max = 500) String note) {
}
