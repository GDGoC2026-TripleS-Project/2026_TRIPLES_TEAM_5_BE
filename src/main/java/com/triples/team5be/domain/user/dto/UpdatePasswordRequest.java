package com.triples.team5be.domain.user.dto;

public record UpdatePasswordRequest(
        String currentPassword,
        String newPassword) {
}