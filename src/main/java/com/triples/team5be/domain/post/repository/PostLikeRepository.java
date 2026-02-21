package com.triples.team5be.domain.post.repository;

import com.triples.team5be.domain.post.entity.Post;
import com.triples.team5be.domain.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Query("select pl.post from PostLike pl where pl.user.id = :userId")
    Page<Post> findLikedPosts(@Param("userId") Long userId, Pageable pageable);
}