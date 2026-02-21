package com.triples.team5be.domain.user.controller;

import com.triples.team5be.domain.user.dto.TrustStatusResponse;
import com.triples.team5be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class TrustStatusController {

    private final UserService userService;

    @GetMapping("/trust-status")
    public ResponseEntity<TrustStatusResponse> getMyTrustStatus(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName()); // JWT subject=userId
        return ResponseEntity.ok(userService.getMyTrustStatus(userId));
    }
}