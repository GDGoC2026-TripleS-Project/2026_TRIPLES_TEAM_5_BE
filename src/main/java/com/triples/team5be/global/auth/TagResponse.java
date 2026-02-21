package com.triples.team5be.global.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class TagResponse {
    private Long postId;
    private String modelVersion;
    private double threshold;
    private List<TagResult> tags;
}
