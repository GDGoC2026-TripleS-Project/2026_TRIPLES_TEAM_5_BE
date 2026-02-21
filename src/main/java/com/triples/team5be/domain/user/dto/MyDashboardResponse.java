package com.triples.team5be.domain.user.dto;

public record MyDashboardResponse(
        String loginId,
        String userName,
        String profileImageUrl,
        Stats stats) {
    public record Stats(
            int postCount,
            int trustScore,
            int tokenBalance) {
    }
}