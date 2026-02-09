package com.triples.team5be.domain.tag.repository;

import com.triples.team5be.domain.tag.entity.PostSituationTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostSituationTagRepository extends JpaRepository<PostSituationTag, Long> {

    // 특정 게시글에 매핑된 상황태그 목록 조회
    List<PostSituationTag> findAllByPostId(Long postId);

    // 특정 게시글에 매핑된 상황태그 전체 삭제 (저장 API에서 "교체" 방식으로 쓰임)
    void deleteAllByPostId(Long postId);
}