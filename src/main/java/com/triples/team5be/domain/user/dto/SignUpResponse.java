package com.triples.team5be.domain.user.dto;

public record SignUpResponse(
        Long userId,
        String loginId
) {
}
