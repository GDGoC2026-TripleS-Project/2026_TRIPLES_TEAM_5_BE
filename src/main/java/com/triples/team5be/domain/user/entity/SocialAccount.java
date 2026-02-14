package com.triples.team5be.domain.user.entity;

import com.triples.team5be.domain.user.enums.Provider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SocialAccount {

    // 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 아이디 (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    // 플랫폼 종류
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private Provider provider;

    // 플랫폼별 고유 식별값
    @Column(nullable = false, unique = true,  updatable = false)
    private String providerId;

    // 소셜계정 이메일
    private String email;

    // 연동 일시
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public SocialAccount(User user, Provider provider, String providerId, String email) {
        this.user = user;
        this.provider = (provider != null) ? provider : Provider.GOOGLE;
        this.providerId = providerId;
        this.email = email;
    }
}
