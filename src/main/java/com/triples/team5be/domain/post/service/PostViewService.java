package com.triples.team5be.domain.post.service;

import com.triples.team5be.domain.post.dto.PostViewCountResponse;
import com.triples.team5be.domain.post.repository.PostRepository;
import com.triples.team5be.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PostViewService {

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;

    private static final Duration VIEW_TTL = Duration.ofHours(24);

    @Transactional
    public PostViewCountResponse recordView(Long postId, String viewerKey) {
        // post 없는 경우는 무조건 404로 (중복조회여도 동일)
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post not found: " + postId);
        }

        String key = "view:post:" + postId + ":" + viewerKey;

        boolean counted = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(key, "1", VIEW_TTL));

        if (counted) {
            postRepository.incrementViewCount(postId);
        }

        int totalViews = postRepository.findViewCountById(postId);
        return new PostViewCountResponse(counted, totalViews);
    }
}