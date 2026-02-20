package com.triples.team5be.domain.user.dto;

public record LoginRequest(
        String loginId,
        String password
) {
}