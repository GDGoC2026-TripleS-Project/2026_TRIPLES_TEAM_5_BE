package com.triples.team5be.global.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TagResult {
    private Long tagId;
    private String tagName;
    private double confidence;
}
