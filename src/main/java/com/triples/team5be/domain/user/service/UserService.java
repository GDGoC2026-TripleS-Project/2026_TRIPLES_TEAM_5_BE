package com.triples.team5be.domain.user.service;

import java.time.LocalDateTime;

import com.triples.team5be.domain.post.repository.PostRepository;
import com.triples.team5be.domain.user.dto.AdminAdjustTrustScoreRequest;
import com.triples.team5be.domain.user.dto.AdminAdjustTrustScoreResponse;
import com.triples.team5be.domain.user.dto.LoginRequest;
import com.triples.team5be.domain.user.dto.LoginResponse;
import com.triples.team5be.domain.user.dto.MyDashboardResponse;
import com.triples.team5be.domain.user.dto.SignUpRequest;
import com.triples.team5be.domain.user.dto.SignUpResponse;
import com.triples.team5be.domain.user.entity.LogoutToken;
import com.triples.team5be.domain.user.dto.TrustStatusResponse;
import com.triples.team5be.domain.user.dto.UpdatePasswordRequest;
import com.triples.team5be.domain.user.dto.UpdatePasswordResponse;
import com.triples.team5be.domain.user.dto.UpdateUserDetailRequest;
import com.triples.team5be.domain.user.dto.UpdateUserDetailResponse;
import com.triples.team5be.domain.user.dto.WithdrawResponse;
import com.triples.team5be.domain.user.entity.TokenBalance;
import com.triples.team5be.domain.user.entity.User;
import com.triples.team5be.domain.user.repository.LogoutTokenRepository;
import com.triples.team5be.domain.user.enums.UserRole;
import com.triples.team5be.domain.user.enums.UserStatus;
import com.triples.team5be.domain.user.repository.UserRepository;
import com.triples.team5be.global.auth.JwtTokenProvider;
import com.triples.team5be.global.exception.BusinessException;
import com.triples.team5be.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.triples.team5be.domain.user.dto.ArchivePostsResponse;
import com.triples.team5be.domain.post.repository.PostLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.List;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int RECOVERED_SCORE = 20;
    private static final int MAX_TRUST_SCORE = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LogoutTokenRepository logoutTokenRepository;
    private final PostRepository postRepository;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        // 중복 검증
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다.");
        }

        // User 엔티티 생성
        User user = User.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .userName(request.userName())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .phoneNumber(request.phoneNumber())
                .thirdPartyConsent(request.thirdPartyConsent())
                .marketingConsent(request.marketingConsent())
                .build();

        // 1:1 관계인 TokenBalance 생성 및 연결
        TokenBalance balance = TokenBalance.builder()
                .user(user)
                .balance(0)
                .build();

        // 양방향 관계 설정
        user.setTokenBalance(balance);

        User savedUser = userRepository.save(user);
        // 최종 저장
        return new SignUpResponse(savedUser.getId(), savedUser.getLoginId());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 유저 조회
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 탈퇴 계정 로그인 차단
        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 잘못되었습니다.");
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getRole().name());

        // 로그인 성공
        return new LoginResponse(
                user.getId(),
                user.getUserName(),
                user.getRole().name(),
                token);
    }

    @Transactional
    public void logout(String token) {

        // 토큰에서 만료 시간 추출
        java.util.Date expirationDate = jwtTokenProvider.getClaims(token).getExpiration();

        // Date를 LocalDateTime으로 변환
        java.time.LocalDateTime expiry = expirationDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();

        // 저장
        logoutTokenRepository.save(new LogoutToken(token, expiry));
    }

    // (사용자) 마이페이지 조회
    // GET /users/me/dashboard
    @Transactional(readOnly = true)
    public MyDashboardResponse getMyDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        int postCount = postRepository.countByAuthorId(userId);

        int tokenBalance = 0;
        if (user.getTokenBalance() != null && user.getTokenBalance().getBalance() != null) {
            tokenBalance = user.getTokenBalance().getBalance();
        }

        int trustScore = user.getTrustScore() != null ? user.getTrustScore() : 0;

        MyDashboardResponse.Stats stats = new MyDashboardResponse.Stats(
                postCount,
                trustScore,
                tokenBalance);

        return new MyDashboardResponse(
                user.getLoginId(),
                user.getUserName(),
                null, // profileImageUrl (현재 User에 없으면 null)
                stats);
    }

    // (사용자) 계정 정보 수정
    // PATCH /users/me/detail
    @Transactional
    public UpdateUserDetailResponse updateMyDetail(Long userId, UpdateUserDetailRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        // userName 검증(들어온 경우만)
        if (request.userName() != null && request.userName().isBlank()) {
            throw new IllegalArgumentException("userName은 공백일 수 없습니다.");
        }

        // phoneNumber 검증/중복체크(들어온 경우만)
        if (request.phoneNumber() != null) {
            if (request.phoneNumber().isBlank()) {
                throw new IllegalArgumentException("phoneNumber는 공백일 수 없습니다.");
            }
            // 내 id 제외하고 중복 체크
            if (userRepository.existsByPhoneNumberAndIdNot(request.phoneNumber(), user.getId())) {
                throw new IllegalArgumentException("이미 등록된 전화번호입니다.");
            }
        }

        // 엔티티에 추가한 updateDetail() 호출 (null이면 기존값 유지)
        user.updateDetail(
                request.userName(),
                request.birthDate(),
                request.gender(),
                request.phoneNumber(),
                request.thirdPartyConsent(),
                request.marketingConsent());

        userRepository.save(user);

        return new UpdateUserDetailResponse(
                user.getId(),
                user.getUserName(),
                user.getBirthDate(),
                user.getGender(),
                user.getPhoneNumber(),
                user.getThirdPartyConsent(),
                user.getMarketingConsent());
    }

    // (사용자) 비밀번호 수정
    // PATCH /users/me/password
    @Transactional
    public UpdatePasswordResponse updateMyPassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("요청 값이 비어있습니다.");
        }
        if (request.currentPassword() == null || request.currentPassword().isBlank()) {
            throw new IllegalArgumentException("currentPassword는 필수입니다.");
        }
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new IllegalArgumentException("newPassword는 필수입니다.");
        }

        if (request.newPassword().length() < 8) {
            throw new IllegalArgumentException("newPassword는 8자 이상이어야 합니다.");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
        }

        String encoded = passwordEncoder.encode(request.newPassword());
        user.changePassword(encoded);

        userRepository.save(user);

        return new UpdatePasswordResponse(user.getId(), "비밀번호 변경 완료");
    }

    // (사용자) 회원 탈퇴
    // DELETE /users/me/detail
    @Transactional
    public WithdrawResponse withdrawMyAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
        }

        user.withdraw();
        userRepository.save(user);

        return new WithdrawResponse(
                user.getId(),
                user.getStatus().name(),
                "회원 탈퇴 완료");
    }

    // (사용자) 내 신뢰도/제재 상태 조회
    // GET /users/me/trust-status
    @Transactional
    public TrustStatusResponse getMyTrustStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        autoRecoverIfNeeded(user);

        boolean canWritePost = user.getStatus() == UserStatus.ACTIVE;

        return new TrustStatusResponse(
                user.getTrustScore(),
                user.getStatus().name(),
                safeInt(user.getBanCount(), 0),
                user.getBanReleaseDate(),
                canWritePost,
                Boolean.TRUE.equals(user.getTokenRestricted()),
                RECOVERED_SCORE);
    }

    // (관리자) 신뢰도 점수 증감 + 자동 제재 트리거(삼진아웃)
    // PATCH /admin/users/{userId}/trust-score
    @Transactional
    public AdminAdjustTrustScoreResponse adjustTrustScore(Long adminId, Long targetUserId,
            AdminAdjustTrustScoreRequest request) {
        if (request == null || request.scoreDelta() == null || request.scoreDelta() == 0) {
            throw new IllegalArgumentException("scoreDelta는 0이 될 수 없습니다.");
        }
        if (request.reason() == null || request.reason().isBlank()) {
            throw new IllegalArgumentException("reason은 필수입니다.");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자만 접근 가능합니다.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("대상 유저를 찾을 수 없습니다."));

        if (target.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        autoRecoverIfNeeded(target);

        int prev = safeInt(target.getTrustScore(), 0);
        int raw = prev + request.scoreDelta();

        if (raw <= 0) {
            target.setTrustScore(0);

            if (target.getStatus() == UserStatus.ACTIVE) {
                applyStrikeBan(target);
            }
        } else {
            int clamped = Math.min(raw, MAX_TRUST_SCORE);
            target.setTrustScore(clamped);
        }

        userRepository.save(target);

        return new AdminAdjustTrustScoreResponse(
                target.getId(),
                prev,
                target.getTrustScore(),
                target.getStatus().name(),
                safeInt(target.getBanCount(), 0),
                target.getBanReleaseDate());
    }

    private void autoRecoverIfNeeded(User user) {
        if (user.getStatus() != UserStatus.BANNED)
            return;

        LocalDateTime release = user.getBanReleaseDate();
        if (release == null)
            return;

        if (!LocalDateTime.now().isBefore(release)) {
            user.setStatus(UserStatus.ACTIVE);
            user.setTrustScore(RECOVERED_SCORE);
            user.setBanReleaseDate(null);
            user.setTokenRestricted(false);

            if (user.getBanCount() == null)
                user.setBanCount(0);
        }
    }

    private void applyStrikeBan(User user) {
        int count = safeInt(user.getBanCount(), 0);
        count = Math.min(count + 1, 3);

        user.setBanCount(count);
        user.setStatus(UserStatus.BANNED);
        user.setTokenRestricted(true);

        if (count == 1) {
            user.setBanReleaseDate(LocalDateTime.now().plusDays(14));
        } else if (count == 2) {
            user.setBanReleaseDate(LocalDateTime.now().plusMonths(1));
        } else {
            user.setBanReleaseDate(null);
        }
    }

    private int safeInt(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }

    private final PostLikeRepository postLikeRepository;

    // (사용자) 아카이브 목록 조회
    // GET
    // /users/me/posts?filter=MY_POST|LIKED&sort=latest|views|likes&page=0&size=20
    @Transactional(readOnly = true)
    public ArchivePostsResponse getMyArchivePosts(Long userId, String filter, String sort, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }
        if (page < 0)
            throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        if (size <= 0)
            throw new IllegalArgumentException("size는 1 이상이어야 합니다.");

        String filterKey = (filter == null || filter.isBlank()) ? "MY_POST" : filter.trim().toUpperCase();
        String sortKey = (sort == null || sort.isBlank()) ? "latest" : sort.trim().toLowerCase();

        Sort sortSpec = switch (sortKey) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "likes" -> Sort.by(Sort.Direction.DESC, "likeCount");
            default -> throw new IllegalArgumentException("sort는 latest/views/likes 중 하나여야 합니다.");
        };

        Pageable pageable = PageRequest.of(page, size, sortSpec);

        Page<com.triples.team5be.domain.post.entity.Post> postPage = switch (filterKey) {
            case "MY_POST" -> postRepository.findByAuthorId(userId, pageable);
            case "LIKED" -> postLikeRepository.findLikedPosts(userId, pageable);
            default -> throw new IllegalArgumentException("filter는 MY_POST/LIKED 중 하나여야 합니다.");
        };

        List<ArchivePostsResponse.Content> content = postPage.getContent().stream().map(post -> {
            boolean anonymous = Boolean.TRUE.equals(post.getIsAnonymous());

            String authorName = anonymous ? "익명" : post.getAuthor().getLoginId();
            Integer trustScore = post.getAuthor().getTrustScore();

            // 태그 가나다순 정렬
            List<String> tags = post.getPostSituationTags().stream()
                    .map(pst -> pst.getTag().getName())
                    .sorted(Comparator.naturalOrder())
                    .toList();

            return new ArchivePostsResponse.Content(
                    post.getTitle(),
                    authorName,
                    trustScore,
                    anonymous,
                    tags,
                    post.getCreatedAt(),
                    post.getUpdatedAt(),
                    post.getViewCount(),
                    post.getLikeCount());
        }).toList();

        ArchivePostsResponse.PageInfo pageInfo = new ArchivePostsResponse.PageInfo(
                postPage.getNumber(),
                postPage.hasNext(),
                postPage.getTotalElements());

        ArchivePostsResponse.Data data = new ArchivePostsResponse.Data(content, pageInfo);

        return new ArchivePostsResponse(200, "조회 성공하였습니다.", data);
    }
}