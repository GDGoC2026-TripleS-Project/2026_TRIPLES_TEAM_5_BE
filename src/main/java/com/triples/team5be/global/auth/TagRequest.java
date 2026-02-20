package com.triples.team5be.global.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagRequest {
    private Long postId;
    private String situation;
    private String choice;
    private String outcome;
    private String reflection;
}
