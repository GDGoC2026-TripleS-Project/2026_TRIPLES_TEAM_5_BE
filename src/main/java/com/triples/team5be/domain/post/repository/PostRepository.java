package com.triples.team5be.domain.post.repository;

import com.triples.team5be.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 기본적인 CRUD(저장, 조회, 삭제) 메서드는 JpaRepository가 제공합니다.
}