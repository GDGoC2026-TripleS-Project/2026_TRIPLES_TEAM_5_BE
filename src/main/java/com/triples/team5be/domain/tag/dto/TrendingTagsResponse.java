package com.triples.team5be.domain.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TrendingTagsResponse {
    private int status;
    private String message;
    private List<String> data;

    public static TrendingTagsResponse success(List<String> data) {
        return new TrendingTagsResponse(200, "인기 태그 조회 성공", data);
    }
}