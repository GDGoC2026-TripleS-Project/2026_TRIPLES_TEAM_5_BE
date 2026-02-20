package com.triples.team5be.domain.user.controller;

import com.triples.team5be.domain.user.dto.AdminAdjustTrustScoreRequest;
import com.triples.team5be.domain.user.dto.AdminAdjustTrustScoreResponse;
import com.triples.team5be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminTrustScoreController {

    private final UserService userService;

    @PatchMapping("/{userId}/trust-score")
    public ResponseEntity<AdminAdjustTrustScoreResponse> adjustTrustScore(
            @PathVariable Long userId,
            @RequestBody AdminAdjustTrustScoreRequest request,
            Authentication authentication) {
        Long adminId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(userService.adjustTrustScore(adminId, userId, request));
    }
}