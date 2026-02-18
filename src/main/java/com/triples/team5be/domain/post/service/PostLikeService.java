package com.triples.team5be.domain.post.service;

import com.triples.team5be.domain.post.dto.PostLikeToggleResponse;
import com.triples.team5be.domain.post.entity.Post;
import com.triples.team5be.domain.post.entity.PostLike;
import com.triples.team5be.domain.post.repository.PostLikeRepository;
import com.triples.team5be.domain.post.repository.PostRepository;
import com.triples.team5be.domain.user.entity.User;
import com.triples.team5be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    /**
     * 토글 방식:
     * - 있으면 취소(delete) + likeCount -1
     * - 없으면 등록(insert) + likeCount +1
     * 좋아요 수가 20의 배수에 도달할 때마다 작성자 trustScore +1
     */
    @Transactional
    public PostLikeToggleResponse toggle(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        return postLikeRepository.findByPostIdAndUserId(postId, userId)
                .map(existing -> {
                    // 좋아요 취소
                    postLikeRepository.delete(existing);

                    postRepository.decrementLikeCount(postId);
                    int totalLikes = postRepository.findLikeCountById(postId);

                    return new PostLikeToggleResponse(false, totalLikes);
                })
                .orElseGet(() -> {
                    // 좋아요 등록
                    try {
                        postLikeRepository.save(PostLike.builder()
                                .postId(postId)
                                .userId(userId)
                                .build());

                        postRepository.incrementLikeCount(postId);

                    } catch (DataIntegrityViolationException e) {
                        // 유니크 제약(중복)으로 이미 들어간 경우 -> 이미 좋아요로 간주
                        int totalLikes = postRepository.findLikeCountById(postId);
                        return new PostLikeToggleResponse(true, totalLikes);
                    }

                    int totalLikes = postRepository.findLikeCountById(postId);

                    // 좋아요 "등록" 시점에만 20개마다 작성자 신뢰도 +1
                    if (totalLikes % 20 == 0) {
                        User author = post.getAuthor(); // Post에는 author(User)가 있음
                        if (author != null && author.getId() != null) {
                            userRepository.incrementTrustScore(author.getId());
                        }
                    }

                    return new PostLikeToggleResponse(true, totalLikes);
                });
    }
}