package com.triples.team5be.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostLikeToggleResponse {
    private boolean liked;
    private int totalLikes;
}