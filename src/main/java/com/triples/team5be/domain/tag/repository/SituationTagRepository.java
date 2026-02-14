package com.triples.team5be.domain.tag.repository;

import com.triples.team5be.domain.tag.entity.SituationTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SituationTagRepository extends JpaRepository<SituationTag, Long> {
    List<SituationTag> findAllByOrderByIdAsc();
}

