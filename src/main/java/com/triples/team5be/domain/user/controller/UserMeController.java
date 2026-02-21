package com.triples.team5be.domain.user.controller;

import com.triples.team5be.domain.user.dto.MyDashboardResponse;
import com.triples.team5be.domain.user.dto.UpdatePasswordRequest;
import com.triples.team5be.domain.user.dto.UpdatePasswordResponse;
import com.triples.team5be.domain.user.dto.UpdateUserDetailRequest;
import com.triples.team5be.domain.user.dto.UpdateUserDetailResponse;
import com.triples.team5be.domain.user.dto.WithdrawResponse;
import com.triples.team5be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.triples.team5be.domain.user.dto.ArchivePostsResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class UserMeController {

    private final UserService userService;

    // 마이페이지 조회: GET /users/me/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<MyDashboardResponse> getMyDashboard(
            @AuthenticationPrincipal String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        MyDashboardResponse response = userService.getMyDashboard(userId);
        return ResponseEntity.ok(response);
    }

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

    // 회원 탈퇴: DELETE /users/me/detail
    @DeleteMapping("/detail")
    public ResponseEntity<WithdrawResponse> withdrawMyAccount(
            @AuthenticationPrincipal String userIdStr) {
        Long userId = Long.parseLong(userIdStr);
        WithdrawResponse response = userService.withdrawMyAccount(userId);
        return ResponseEntity.ok(response);
    }

    // 아카이브 목록 조회
    @GetMapping("/posts")
    public ResponseEntity<ArchivePostsResponse> getMyArchivePosts(
            @AuthenticationPrincipal String userIdStr,
            @RequestParam(defaultValue = "MY_POST") String filter,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = Long.parseLong(userIdStr);
        ArchivePostsResponse response = userService.getMyArchivePosts(userId, filter, sort, page, size);
        return ResponseEntity.ok(response);
    }
}