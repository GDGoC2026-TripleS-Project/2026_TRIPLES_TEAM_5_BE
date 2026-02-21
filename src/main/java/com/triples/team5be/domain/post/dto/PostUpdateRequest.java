package com.triples.team5be.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostUpdateRequest {
    private String title;
    private String situation;
    private String action;
    private String retrospective;
}

