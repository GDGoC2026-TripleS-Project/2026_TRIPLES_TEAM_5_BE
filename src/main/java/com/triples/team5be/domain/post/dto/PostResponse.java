package com.triples.team5be.domain.post.dto;

import com.triples.team5be.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String authorDisplayName; // 익명 여부에 따라 결정될 필드
    private String title;
    private String situation;
    private String action;
    private String retrospective;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;

    public static PostResponse from(Post post) {

        // 익명 여부 체크
        String displayName = post.getIsAnonymous() ? "익명" : post.getAuthor().getLoginId();

        return PostResponse.builder()
                .id(post.getId())
                .authorDisplayName(displayName)
                .title(post.getTitle())
                .situation(post.getSituation())
                .action(post.getAction())
                .retrospective(post.getRetrospective())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
