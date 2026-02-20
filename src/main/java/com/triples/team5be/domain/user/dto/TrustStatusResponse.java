package com.triples.team5be.domain.user.dto;

import java.time.LocalDateTime;

public record TrustStatusResponse(
        Integer trustScore,
        String userStatus,
        Integer banCount,
        LocalDateTime banReleaseDate,
        boolean canWritePost,
        boolean tokenRestricted,
        Integer recoveredScore) {
}