package com.triples.team5be.domain.user.entity;

import com.triples.team5be.domain.user.enums.TokenType;
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
public class TokenHistory {

    // 이력 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 변동 주체 (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 변동 사유
    @Enumerated(EnumType.STRING)
    private TokenType type;

    // 변동 개수
    @Column(nullable = false)
    private Integer amount;

    // 변동 후 잔여 토큰
    @Column(nullable = false)
    private Integer balanceAfter;

    // 상세 사유
    private String description;

    // 발생 시점
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TokenHistory(User user, TokenType type, Integer amount, Integer balanceAfter, String description) {
        this.user = user;
        this.type = (type != null) ? type : TokenType.CHARGE;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }
}
