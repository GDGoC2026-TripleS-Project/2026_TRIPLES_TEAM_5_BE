package com.triples.team5be.domain.user.dto;

import com.triples.team5be.domain.user.enums.Gender;

import java.time.LocalDate;

public record UpdateUserDetailResponse(
        Long userId,
        String userName,
        LocalDate birthDate,
        Gender gender,
        String phoneNumber,
        Boolean thirdPartyConsent,
        Boolean marketingConsent) {
}