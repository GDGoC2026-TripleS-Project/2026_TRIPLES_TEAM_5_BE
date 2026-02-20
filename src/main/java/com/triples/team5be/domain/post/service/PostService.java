package com.triples.team5be.domain.post.service;

import com.triples.team5be.domain.post.dto.PostCreateRequest;
import com.triples.team5be.domain.post.dto.PostResponse;
import com.triples.team5be.domain.post.dto.PostUpdateRequest;
import com.triples.team5be.domain.post.entity.Post;
import com.triples.team5be.domain.post.enums.PostStatus;
import com.triples.team5be.domain.post.repository.PostRepository;
import com.triples.team5be.domain.user.entity.User;
import com.triples.team5be.domain.user.repository.UserRepository;
import com.triples.team5be.global.error.BusinessException;
import com.triples.team5be.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    // AI 서버 주소?
    private final String AI_TAGGING_URL = "http://localhost:8000/ai/tagging";
    private final String AI_DB_SAVE_URL = "http://localhost:8000/ai/db-save";

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

        sendToAiTagging(savedPost);
        sendToAiDbSave(savedPost);

        return savedPost.getId();
    }

    private void sendToAiTagging(Post post) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("postId", post.getId());
            body.put("situation", post.getSituation());
            body.put("choice", post.getAction());
            body.put("outcome", post.getRetrospective());
            body.put("reflection", post.getRetrospective());

            executePostRequest(AI_TAGGING_URL, body);
        } catch (Exception e) {
            System.err.println("AI Tagging 호출 실패: " + e.getMessage());
        }
    }

    private void sendToAiDbSave(Post post) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("postId", post.getId());
            body.put("situation", post.getSituation());
            body.put("choice", post.getAction());

            executePostRequest(AI_DB_SAVE_URL, body);
        } catch (Exception e) {
            System.err.println("AI DB Save 호출 실패: " + e.getMessage());
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
