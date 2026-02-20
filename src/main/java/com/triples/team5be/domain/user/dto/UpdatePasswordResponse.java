package com.triples.team5be.domain.user.dto;

public record UpdatePasswordResponse(
        Long userId,
        String message) {
}