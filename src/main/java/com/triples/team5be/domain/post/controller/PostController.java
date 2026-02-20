package com.triples.team5be.domain.post.controller;

import com.triples.team5be.domain.post.dto.PostCreateRequest;
import com.triples.team5be.domain.post.dto.PostResponse;
import com.triples.team5be.domain.post.dto.PostUpdateRequest;
import com.triples.team5be.domain.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Long> create(@Valid @RequestBody PostCreateRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        String loginId = userDetails.getUsername();

        // 현재 로그인된 유저의 아이디를 가져옴
        Long postId = postService.createPost(request, loginId);
        return ResponseEntity.ok(postId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestBody PostUpdateRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        postService.updatePost(id, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        postService.deletePost(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

}
