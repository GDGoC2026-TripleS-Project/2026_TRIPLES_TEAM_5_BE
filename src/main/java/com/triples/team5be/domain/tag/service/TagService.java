package com.triples.team5be.domain.tag.service;

import com.triples.team5be.domain.tag.dto.PostSituationTagResponse;
import com.triples.team5be.domain.tag.dto.SavePostSituationTagsRequest;
import com.triples.team5be.domain.tag.dto.SituationTagResponse;
import com.triples.team5be.domain.tag.entity.PostSituationTag;
import com.triples.team5be.domain.tag.entity.SituationTag;
import com.triples.team5be.domain.tag.repository.PostSituationTagRepository;
import com.triples.team5be.domain.tag.repository.SituationTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final SituationTagRepository situationTagRepository;
    private final PostSituationTagRepository postSituationTagRepository;

    // GET /tags/situations
    public List<SituationTagResponse> getSituationTags() {
        return situationTagRepository.findAllByOrderByIdAsc()
                .stream()
                .map(t -> new SituationTagResponse(t.getId(), t.getName(), t.getIsActive()))
                .toList();
    }

    // GET /posts/{postId}/tags/situations
    public List<PostSituationTagResponse> getPostSituationTags(Long postId) {
        return postSituationTagRepository.findAllByPostId(postId)
                .stream()
                .map(PostSituationTag::getTag)
                .map(t -> new PostSituationTagResponse(t.getId(), t.getName()))
                .toList();
    }

    // POST /posts/{postId}/tags/situations (교체 방식)
    @Transactional
    public void savePostSituationTags(Long postId, SavePostSituationTagsRequest request) {
        // 1) 기존 매핑 삭제
        postSituationTagRepository.deleteAllByPostId(postId);

        // 2) 요청 tagIds -> SituationTag 조회
        List<Long> tagIds = (request.tagIds() == null) ? List.of() : request.tagIds();
        List<SituationTag> tags = situationTagRepository.findAllById(tagIds);

        if (tags.size() != tagIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 tagId가 포함되어 있습니다.");
        }

        // 3) 매핑 저장
        List<PostSituationTag> mappings = new ArrayList<>();
        for (SituationTag tag : tags) {
            mappings.add(PostSituationTag.builder()
                    .postId(postId)
                    .tag(tag)
                    .build());
        }
        postSituationTagRepository.saveAll(mappings);
    }
}