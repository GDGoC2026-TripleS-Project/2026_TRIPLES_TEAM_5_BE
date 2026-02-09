package com.triples.team5be.domain.tag.controller;

import com.triples.team5be.domain.tag.dto.PostSituationTagResponse;
import com.triples.team5be.domain.tag.dto.SavePostSituationTagsRequest;
import com.triples.team5be.domain.tag.dto.SituationTagResponse;
import com.triples.team5be.domain.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // GET /tags/situations
    @GetMapping("/tags/situations")
    public List<SituationTagResponse> getSituationTags() {
        return tagService.getSituationTags();
    }

    // GET /posts/{postId}/tags/situations
    @GetMapping("/posts/{postId}/tags/situations")
    public List<PostSituationTagResponse> getPostSituationTags(@PathVariable Long postId) {
        return tagService.getPostSituationTags(postId);
    }

    // POST /posts/{postId}/tags/situations
    @PostMapping("/posts/{postId}/tags/situations")
    public ResponseEntity<Void> savePostSituationTags(
            @PathVariable Long postId,
            @RequestBody SavePostSituationTagsRequest request) {
        tagService.savePostSituationTags(postId, request);
        return ResponseEntity.ok().build();
    }
}