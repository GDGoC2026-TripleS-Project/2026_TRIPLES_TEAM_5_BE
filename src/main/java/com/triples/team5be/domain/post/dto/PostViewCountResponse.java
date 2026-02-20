package com.triples.team5be.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostViewCountResponse {
    private boolean counted;
    private int totalViews;
}