package com.triples.team5be.domain.user.dto;

public record LoginResponse(
        Long userId,
        String userName,
        String role, // 권한 정보 등 포함
        String accessToken
) {
    // 임시 생성자
    public LoginResponse(Long userId, String userName, String role) {
        this(userId, userName, role, "temporary-token-value");
    }
}
