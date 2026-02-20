package com.triples.team5be.domain.user.controller;

import com.triples.team5be.domain.user.dto.UpdatePasswordRequest;
import com.triples.team5be.domain.user.dto.UpdatePasswordResponse;
import com.triples.team5be.domain.user.dto.UpdateUserDetailRequest;
import com.triples.team5be.domain.user.dto.UpdateUserDetailResponse;
import com.triples.team5be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class UserMeController {

    private final UserService userService;

    @PatchMapping("/detail")
    public ResponseEntity<UpdateUserDetailResponse> updateMyDetail(
            @AuthenticationPrincipal String userIdStr,
            @RequestBody UpdateUserDetailRequest request) {
        Long userId = Long.parseLong(userIdStr);
        UpdateUserDetailResponse response = userService.updateMyDetail(userId, request);
        return ResponseEntity.ok(response);
    }

    // 비밀번호 수정: PATCH /users/me/password
    @PatchMapping("/password")
    public ResponseEntity<UpdatePasswordResponse> updateMyPassword(
            @AuthenticationPrincipal String userIdStr,
            @RequestBody UpdatePasswordRequest request) {
        Long userId = Long.parseLong(userIdStr);
        UpdatePasswordResponse response = userService.updateMyPassword(userId, request);
        return ResponseEntity.ok(response);
    }
}