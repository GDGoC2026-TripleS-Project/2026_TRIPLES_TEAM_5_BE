package com.triples.team5be.domain.user.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ArchivePostsResponse(
        int status,
        String message,
        Data data) {
    public record Data(
            List<Content> content,
            PageInfo pageInfo) {
    }

    public record Content(
            String title,
            String authorName,
            Integer trustScore,
            Boolean isAnonymous,
            List<String> tags,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Integer viewCount,
            Integer likeCount) {
    }

    public record PageInfo(
            int currentPage,
            boolean hasNext,
            long totalElements) {
    }
}