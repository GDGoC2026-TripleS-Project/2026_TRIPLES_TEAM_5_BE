package com.triples.team5be.domain.post.service;

import com.triples.team5be.domain.post.dto.PostCreateRequest;
import com.triples.team5be.domain.post.dto.PostResponse;
import com.triples.team5be.domain.post.dto.PostUpdateRequest;
import com.triples.team5be.domain.post.entity.Post;
import com.triples.team5be.domain.tag.entity.PostSituationTag;
import com.triples.team5be.domain.tag.entity.SituationTag;
import com.triples.team5be.domain.post.enums.PostStatus;
import com.triples.team5be.domain.post.repository.PostRepository;
import com.triples.team5be.domain.tag.repository.PostSituationTagRepository;
import com.triples.team5be.domain.tag.repository.SituationTagRepository;
import com.triples.team5be.domain.user.entity.User;
import com.triples.team5be.domain.user.repository.UserRepository;
import com.triples.team5be.global.auth.TagResponse;
import com.triples.team5be.global.auth.TagResult;
import com.triples.team5be.global.exception.BusinessException;
import com.triples.team5be.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SituationTagRepository tagRepository;
    private final PostSituationTagRepository postTagRepository;
    private final RestTemplate restTemplate;

    // AI 서버 주소?
    private final String AI_SERVER_URL = "http://ai-server-ip:8000";

    @Transactional
    public Long createPost(PostCreateRequest request, String loginId) {

        // 현재 로그인한 사용자 정보 가져오기
        User author = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 엔티티 생성
        Post post = Post.builder()
                .author(author)
                .title(request.getTitle())
                .situation(request.getSituation())
                .action(request.getAction())
                .retrospective(request.getRetrospective())
                .isPremium(request.getIsPremium())
                .requiredToken(request.getRequiredToken())
                .isAnonymous(request.getIsAnonymous())
                .status(PostStatus.PUBLISHED) // 기본값 설정
                .build();

        Post savedPost = postRepository.save(post);

        processAiTasks(savedPost);

        return savedPost.getId();
    }

    public void processAiTasks(Post post) {
        // AI 서버로 보낼 요청 객체
        Map<String, Object> request = new HashMap<>();
        request.put("postId", post.getId());
        request.put("situation", post.getSituation());
        request.put("action", post.getAction());
        request.put("reflection", post.getRetrospective());

        try {
            // 태그 분류 요청
            String tagUrl = AI_SERVER_URL + "/api/ai/tag";
            ResponseEntity<TagResponse> tagResponse = restTemplate.postForEntity(tagUrl, request, TagResponse.class);

            if (tagResponse.getStatusCode().is2xxSuccessful()) {
                // 받은 태그 ID들로 PostTag 매핑 저장
                savePostTags(post, tagResponse.getBody().getTags());
            }

            // 임베딩 및 추천 데이터 갱신 요청
            String recommUrl = AI_SERVER_URL + "/api/ai/recommend";
            restTemplate.postForEntity(recommUrl, request, String.class);

        } catch (Exception e) {
            log.error("AI 연동 중 오류 발생: {}", e.getMessage());
        }
    }

    private void savePostTags(Post post, List<TagResult> tagResults) {
        for (TagResult result : tagResults) {
            SituationTag tag = tagRepository.findById(result.getTagId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND)); //

            PostSituationTag postTag = PostSituationTag.builder()
                    .post(post)
                    .tag(tag)
                    .build();

            postTagRepository.save(postTag);
        }
    }

    private void executePostRequest(String url, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, entity, String.class);
    }

    @Transactional // 조회수 증가를 위해 Dirty Checking 활용
    public PostResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 조회수 증가 로직
        post.incrementViewCount();

        return PostResponse.from(post);
    }

    @Transactional
    public void updatePost(Long postId, PostUpdateRequest request, String loginId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 본인 확인
        if (!post.getAuthor().getLoginId().equals(loginId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED); // 권한 없음 에러
        }

        post.update(request.getTitle(), request.getSituation(), request.getAction(), request.getRetrospective());
    }

    @Transactional
    public void deletePost(Long postId, String loginId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getAuthor().getLoginId().equals(loginId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        postRepository.delete(post);
    }
}
