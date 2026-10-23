package com.findsomeone.dto;

import com.findsomeone.entity.ConnectionPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank @Size(max = 5000) String body,
        @NotNull ConnectionPurpose purpose) {
}
