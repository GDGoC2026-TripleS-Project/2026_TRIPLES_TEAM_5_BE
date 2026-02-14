package com.triples.team5be.domain.post.entity;

import com.triples.team5be.domain.post.enums.PostStatus;
import com.triples.team5be.domain.report.entity.Report;
import com.triples.team5be.domain.user.entity.User;
import com.triples.team5be.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post")
public class Post extends BaseTimeEntity {

    // 게시글 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 아이디 (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 제목
    @Column(nullable = false, length = 50)
    private String title;

    // 상황
    @Column(columnDefinition = "TEXT", nullable = false)
    private String situation;

    // 행동
    @Column(columnDefinition = "TEXT", nullable = false)
    private String action;

    // 회고
    @Column(columnDefinition = "TEXT", nullable = false)
    private String retrospective;

    // 프리미엄 구독 사용자 여부
    @Column(nullable = false)
    private Boolean isPremium;

    // 사용한 토큰의 개수
    private Integer requiredToken;

    // 익명 여부
    @Column(nullable = false)
    private Boolean isAnonymous;

    // 글 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    // 조회수
    @Column(nullable = false)
    private Integer viewCount;

    // 좋아요 수
    @Column(nullable = false)
    private Integer likeCount;

    // N:M (Tag)
    @OneToMany(mappedBy = "post")
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "targetPost")
    private List<Report> reports = new ArrayList<>();

    @Builder
    public Post(User author, String title, String situation, String action,
                String retrospective, Boolean isPremium, Integer requiredToken,
                Boolean isAnonymous, PostStatus status) {
        this.author = author;
        this.title = title;
        this.situation = situation;
        this.action = action;
        this.retrospective = retrospective;
        this.isPremium = (isPremium != null) ? isPremium : false;
        this.requiredToken = (requiredToken != null) ? requiredToken : 0;
        this.isAnonymous = (isAnonymous != null) ? isAnonymous : false;
        this.status = (status != null) ? status : PostStatus.PUBLISHED;
        this.viewCount = 0;
        this.likeCount = 0;
    }
}
