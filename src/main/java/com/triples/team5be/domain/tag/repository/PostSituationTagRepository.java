package com.triples.team5be.domain.tag.repository;

import com.triples.team5be.domain.post.enums.PostStatus;
import com.triples.team5be.domain.tag.entity.PostSituationTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostSituationTagRepository extends JpaRepository<PostSituationTag, Long> {

    // 특정 게시글에 매핑된 상황태그 목록 조회
    List<PostSituationTag> findAllByPost_Id(Long postId);

    // 특정 게시글에 매핑된 상황태그 전체 삭제 (저장 API에서 "교체" 방식으로 쓰임)
    void deleteAllByPost_Id(Long postId);

    // 이번 주 인기 태그 조회 (Top N은 Pageable로 제한)
    @Query("""
                select pst.tag.name
                from PostSituationTag pst
                join pst.post p
                where p.createdAt >= :start and p.createdAt < :end
                  and p.status = :status
                group by pst.tag.id, pst.tag.name
                order by count(pst.id) desc
            """)
    List<String> findTrendingTagNames(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") PostStatus status,
            Pageable pageable);
}