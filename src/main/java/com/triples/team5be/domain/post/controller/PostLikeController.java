package com.triples.team5be.domain.post.controller;

import com.triples.team5be.domain.post.dto.PostLikeToggleResponse;
import com.triples.team5be.domain.post.service.PostLikeService;
import com.triples.team5be.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/likes")
    public ResponseEntity<PostLikeToggleResponse> toggleLike(
            @PathVariable Long postId,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Long userId;
        try {
            // principal = userId 문자열 (JwtTokenProvider에서 subject=userId)
            userId = Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid token subject.");
        }

        return ResponseEntity.ok(postLikeService.toggle(postId, userId));
    }
}