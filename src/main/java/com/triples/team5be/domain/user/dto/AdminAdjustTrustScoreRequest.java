package com.triples.team5be.domain.user.dto;

public record AdminAdjustTrustScoreRequest(
        Integer scoreDelta,
        String reason,
        String note) {
}