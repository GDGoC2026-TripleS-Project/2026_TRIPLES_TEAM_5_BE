package com.triples.team5be.domain.post.repository;

import com.triples.team5be.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying
    @Query("update Post p set p.likeCount = p.likeCount + 1 where p.id = :postId")
    int incrementLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("update Post p set p.likeCount = case when p.likeCount > 0 then p.likeCount - 1 else 0 end where p.id = :postId")
    int decrementLikeCount(@Param("postId") Long postId);

    @Query("select p.likeCount from Post p where p.id = :postId")
    int findLikeCountById(@Param("postId") Long postId);
}