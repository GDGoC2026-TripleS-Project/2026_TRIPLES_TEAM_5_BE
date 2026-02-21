package com.triples.team5be.domain.post.controller;

import com.triples.team5be.domain.post.dto.PostViewCountResponse;
import com.triples.team5be.domain.post.service.PostViewService;
import com.triples.team5be.global.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostViewController {

    private final PostViewService postViewService;

    @PostMapping("/{postId}/views")
    public ResponseEntity<PostViewCountResponse> recordView(
            @PathVariable Long postId,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        String viewerKey;

        // 로그인 유저면 userId 기반 중복방지
        if (authentication != null && authentication.getName() != null) {
            try {
                Long userId = Long.parseLong(authentication.getName()); // subject=userId
                viewerKey = "user:" + userId;
            } catch (NumberFormatException e) {
                throw new UnauthorizedException("Invalid token subject.");
            }
        } else {
            // 익명 유저면 anonymousId 쿠키 기반 중복방지
            String anonymousId = getCookieValue(request, "anonymousId");
            if (anonymousId == null) {
                anonymousId = UUID.randomUUID().toString();
                Cookie cookie = new Cookie("anonymousId", anonymousId);
                cookie.setPath("/");
                cookie.setMaxAge(60 * 60 * 24 * 365); // 1년 (원하면 조정)
                response.addCookie(cookie);
            }
            viewerKey = "anon:" + anonymousId;
        }

        return ResponseEntity.ok(postViewService.recordView(postId, viewerKey));
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null)
            return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName()))
                return c.getValue();
        }
        return null;
    }
}