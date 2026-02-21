package com.triples.team5be.domain.tag.entity;

import com.triples.team5be.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_situation_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostSituationTag {

    // 기본키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글 아이디 (Post와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 태그 고유 ID (SituationTag와 N:1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private SituationTag tag;
}
