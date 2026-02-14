package com.triples.team5be.domain.user.entity;

import com.triples.team5be.domain.user.enums.ReportStatus;
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
public class Report {

    // 신고 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고자 (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, updatable = false)
    private User reporter;

    // 신고 대상 게시글 (Post와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_post_id", nullable = false, updatable = false)
    private Post targetPost;

    // 피신고자 (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false,  updatable = false)
    private User targetUser;

    // 신고 사유
    @Column(nullable = false, length = 50)
    private String reason;

    // 신고 상세 내용
    @Column(columnDefinition = "TEXT")
    private String details;

    // 처리 상태
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    // 신고 일시
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 1:1 (AdminAction)
    @OneToOne(mappedBy = "report")
    private AdminAction adminAction;

    @Builder
    public Report(User reporter, Post targetPost, User targetUser, String reason, String details, ReportStatus status) {
        this.reporter = reporter;
        this.targetPost = targetPost;
        this.targetUser = targetUser;
        this.reason = reason;
        this.details = details;
        this.status = (status != null) ? status : ReportStatus.RECEIVED;
    }

}
