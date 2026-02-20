package com.triples.team5be.domain.user.dto;

import java.time.LocalDateTime;

public record AdminAdjustTrustScoreResponse(
        Long userId,
        Integer previousScore,
        Integer newScore,
        String userStatus,
        Integer banCount,
        LocalDateTime banReleaseDate) {
}