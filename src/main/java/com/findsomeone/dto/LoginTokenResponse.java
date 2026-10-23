package com.findsomeone.dto;

public record LoginTokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        Long userId) {
}
