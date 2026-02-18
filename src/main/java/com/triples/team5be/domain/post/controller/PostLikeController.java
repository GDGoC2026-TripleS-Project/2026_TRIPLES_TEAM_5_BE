package com.triples.team5be.domain.post.controller;

import com.triples.team5be.domain.post.dto.PostLikeToggleResponse;
import com.triples.team5be.domain.post.service.PostLikeService;
import com.triples.team5be.domain.user.entity.User;
import com.triples.team5be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final UserRepository userRepository;

    /**
     * 글 좋아요 토글
     * POST /api/posts/{postId}/likes
     * Authorization: Bearer {token}
     */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<PostLikeToggleResponse> toggleLike(
            @PathVariable Long postId,
            Authentication authentication) {
        // authentication.getName()이 loginId라고 가정 (프로젝트 JWT 설정에 따라 바뀔 수 있음)
        String loginId = authentication.getName();

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found by loginId: " + loginId));

        PostLikeToggleResponse response = postLikeService.toggle(postId, user.getId());
        return ResponseEntity.ok(response);
    }
}