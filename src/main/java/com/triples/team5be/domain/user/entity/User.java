package com.triples.team5be.domain.user.entity;

import com.triples.team5be.domain.post.entity.Post;
import com.triples.team5be.domain.report.entity.Report;
import com.triples.team5be.domain.user.enums.Gender;
import com.triples.team5be.domain.user.enums.UserPlan;
import com.triples.team5be.domain.user.enums.UserRole;
import com.triples.team5be.domain.user.enums.UserStatus;
import com.triples.team5be.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users") // user는 DB 예약어인 경우가 많아 users로 변경
public class User extends BaseTimeEntity {

    // 사용자 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 아이디
    @Column(nullable = false, unique = true, length = 10)
    private String loginId;

    // 비밀번호
    @Column(nullable = false, length = 255) // 암호화된 비밀번호 저장
    private String password;

    // 사용자 이름
    @Column(nullable = false, length = 30)
    private String userName;

    // 생년월일
    @Column(nullable = false)
    private LocalDate birthDate;

    // 성별
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    // 휴대폰 번호
    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    // 개인정보 제3자 제공 동의 여부
    @Column(nullable = false)
    private Boolean thirdPartyConsent;

    // 마케팅 정보 수신 동의 여부
    @Column(nullable = false)
    private Boolean marketingConsent;

    // 계정 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    // 계정 권한
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // 사용자 등급
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserPlan plan;

    // 프리미엄 구독 만료 일시
    private LocalDateTime planExpiresAt;

    // 신뢰도 점수
    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private Integer trustScore; // 기본 신뢰 점수

    // 신뢰도 제재/제한 상태(추가)
    // 삼진아웃 제재 횟수 (0~3)
    @Column(nullable = false)
    private Integer banCount = 0;

    // 제재 해제 일시 (영구정지면 null)
    @Column
    private LocalDateTime banReleaseDate;

    // 토큰 사용 제한 여부
    @Column(nullable = false)
    private Boolean tokenRestricted = false;

    // 아이디 변경 일시
    // 아이디 변경 주기 제한용
    private LocalDateTime loginIdUpdatedAt;

    // 1:N (Post)
    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();

    // 1:N (TokenHistory)
    @OneToMany(mappedBy = "user")
    private List<TokenHistory> tokenHistories = new ArrayList<>();

    // 1:N (Report)
    @OneToMany(mappedBy = "reporter")
    private List<Report> reports = new ArrayList<>();

    // 1:N (SocialAccount)
    @OneToMany(mappedBy = "user")
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    // 1:1 (TokenBalance 식별자 관계)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private TokenBalance tokenBalance;

    // 1:N (AdminAction)
    // 관리자, 대상자 각각 참조
    @OneToMany(mappedBy = "admin")
    private List<AdminAction> performedActions = new ArrayList<>();

    @OneToMany(mappedBy = "targetUser")
    private List<AdminAction> receivedPenalties = new ArrayList<>();

    public void setTokenBalance(TokenBalance tokenBalance) {
        if (tokenBalance == null) {
            this.tokenBalance = null;
            return;
        }
        this.tokenBalance = tokenBalance;
        // tokenBalance.setUser(this);
    }

    // 상태 변경용 메서드(서비스에서 사용)
    public void setTrustScore(Integer trustScore) {
        this.trustScore = trustScore;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setBanCount(Integer banCount) {
        this.banCount = banCount;
    }

    public void setBanReleaseDate(LocalDateTime banReleaseDate) {
        this.banReleaseDate = banReleaseDate;
    }

    public void setTokenRestricted(Boolean tokenRestricted) {
        this.tokenRestricted = tokenRestricted;
    }

    // 비밀번호 변경용 (encodedPassword를 넣어주세요)
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 계정 정보 수정(PATCH)용 - null이 아닌 값만 반영
    public void updateDetail(
            String userName,
            LocalDate birthDate,
            Gender gender,
            String phoneNumber,
            Boolean thirdPartyConsent,
            Boolean marketingConsent) {
        if (userName != null)
            this.userName = userName;
        if (birthDate != null)
            this.birthDate = birthDate;
        if (gender != null)
            this.gender = gender;
        if (phoneNumber != null)
            this.phoneNumber = phoneNumber;
        if (thirdPartyConsent != null)
            this.thirdPartyConsent = thirdPartyConsent;
        if (marketingConsent != null)
            this.marketingConsent = marketingConsent;
    }

    @Builder
    public User(String loginId, String password, String userName, LocalDate birthDate,
            Gender gender, String phoneNumber, Boolean thirdPartyConsent, Boolean marketingConsent,
            UserStatus status, UserRole role, UserPlan plan, Integer trustScore) {
        this.loginId = loginId;
        this.password = password;
        this.userName = userName;
        this.birthDate = birthDate;
        this.gender = (gender != null) ? gender : Gender.MALE;
        this.phoneNumber = phoneNumber;
        this.thirdPartyConsent = thirdPartyConsent;
        this.marketingConsent = marketingConsent;
        this.status = (status != null) ? status : UserStatus.ACTIVE;
        this.role = (role != null) ? role : UserRole.USER;
        this.plan = (plan != null) ? plan : UserPlan.FREE;
        this.trustScore = (trustScore != null) ? trustScore : 50;

        // 제재/제한 기본값 (신규 유저 기본)
        this.banCount = 0;
        this.banReleaseDate = null;
        this.tokenRestricted = false;
    }
}