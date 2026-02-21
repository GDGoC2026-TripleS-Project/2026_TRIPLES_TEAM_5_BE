package com.triples.team5be.domain.user.dto;

public record WithdrawResponse(
        Long userId,
        String status,
        String message) {
}