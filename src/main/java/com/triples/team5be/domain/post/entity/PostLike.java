package com.triples.team5be.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PostLike", uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_like_post_user", columnNames = { "postId", "userId" })
}, indexes = {
        @Index(name = "idx_post_like_post", columnList = "postId"),
        @Index(name = "idx_post_like_user", columnList = "userId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long userId;

    @Builder
    private PostLike(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }
}
