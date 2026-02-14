package com.triples.team5be.domain.user.entity;

import com.triples.team5be.domain.user.enums.ActionType;
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
public class AdminAction {

    // 제재 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관된 신고 ID (Report와 1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    // 관리자 ID (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    // 제재 대상자 ID (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    // 조치 종류
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    // 차감된 신뢰점수
    private Integer penaltyScore;

    // 조치 사유
    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    // 제재 만료 일시
    private LocalDateTime expiresAt;

    // 조치 일시
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AdminAction(Report report, User admin, User targetUser, ActionType actionType,
                       Integer penaltyScore, String reason, LocalDateTime expiresAt) {
        this.report = report;
        this.admin = admin;
        this.targetUser = targetUser;
        this.actionType = (actionType != null) ? actionType : ActionType.WARNING;
        this.penaltyScore = (penaltyScore != null) ? penaltyScore : 0;
        this.reason = reason;
        this.expiresAt = expiresAt;
    }
}
