package com.triples.team5be.domain.user.service;

import com.triples.team5be.domain.user.dto.AdminAdjustTrustScoreRequest;
import com.triples.team5be.domain.user.dto.AdminAdjustTrustScoreResponse;
import com.triples.team5be.domain.user.dto.LoginRequest;
import com.triples.team5be.domain.user.dto.LoginResponse;
import com.triples.team5be.domain.user.dto.SignUpRequest;
import com.triples.team5be.domain.user.dto.SignUpResponse;
import com.triples.team5be.domain.user.dto.TrustStatusResponse;
import com.triples.team5be.domain.user.dto.UpdatePasswordRequest;
import com.triples.team5be.domain.user.dto.UpdatePasswordResponse;
import com.triples.team5be.domain.user.dto.UpdateUserDetailRequest;
import com.triples.team5be.domain.user.dto.UpdateUserDetailResponse;
import com.triples.team5be.domain.user.entity.TokenBalance;
import com.triples.team5be.domain.user.entity.User;
import com.triples.team5be.domain.user.enums.UserRole;
import com.triples.team5be.domain.user.enums.UserStatus;
import com.triples.team5be.domain.user.repository.UserRepository;
import com.triples.team5be.global.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int RECOVERED_SCORE = 20;
    private static final int MAX_TRUST_SCORE = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 잘못되었습니다."));

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

    // (사용자) 계정 정보 수정
    // PATCH /users/me/detail
    @Transactional
    public UpdateUserDetailResponse updateMyDetail(Long userId, UpdateUserDetailRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

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

        userRepository.save(user); // 프로젝트 스타일에 맞춰 명시 save

        return new UpdateUserDetailResponse(
                user.getId(),
                user.getUserName(),
                user.getBirthDate(),
                user.getGender(),
                user.getPhoneNumber(),
                user.getThirdPartyConsent(),
                user.getMarketingConsent());
    }

    // ✅ (사용자) 비밀번호 수정
    // PATCH /users/me/password
    @Transactional
    public UpdatePasswordResponse updateMyPassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (request == null) {
            throw new IllegalArgumentException("요청 값이 비어있습니다.");
        }
        if (request.currentPassword() == null || request.currentPassword().isBlank()) {
            throw new IllegalArgumentException("currentPassword는 필수입니다.");
        }
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new IllegalArgumentException("newPassword는 필수입니다.");
        }

        // 새 비밀번호 최소 길이 (팀 정책 있으면 그걸로 수정)
        if (request.newPassword().length() < 8) {
            throw new IllegalArgumentException("newPassword는 8자 이상이어야 합니다.");
        }

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 동일 비밀번호 방지(선택)
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
        }

        // 변경 (User 엔티티: changePassword 필요)
        String encoded = passwordEncoder.encode(request.newPassword());
        user.changePassword(encoded);

        userRepository.save(user);

        return new UpdatePasswordResponse(user.getId(), "비밀번호 변경 완료");
    }

    // (사용자) 내 신뢰도/제재 상태 조회
    // GET /users/me/trust-status
    @Transactional
    public TrustStatusResponse getMyTrustStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 조회 시점에 자동 해제(해제일 지나면 ACTIVE로 + trustScore 회복)
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

        // 관리자 권한 체크 (프로젝트에서 role을 쓰고 있으니 DB role로 검사)
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자만 접근 가능합니다.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("대상 유저를 찾을 수 없습니다."));

        // 조정 전에 자동 해제 처리(기간 만료된 밴이면 풀고 회복 점수로)
        autoRecoverIfNeeded(target);

        int prev = safeInt(target.getTrustScore(), 0);
        int raw = prev + request.scoreDelta();

        // @Min(0) 깨지지 않게: 음수 저장 금지
        if (raw <= 0) {
            // 점수는 0으로만 저장
            target.setTrustScore(0);

            // ACTIVE에서 0 이하로 "떨어지는 순간"만 strike 증가(권장)
            if (target.getStatus() == UserStatus.ACTIVE) {
                applyStrikeBan(target);
            }
            // 이미 BANNED인 상태면 banCount는 추가로 올리지 않음(중복 strike 방지)
        } else {
            // 0 초과면 0~100 범위로 clamp
            int clamped = Math.min(raw, MAX_TRUST_SCORE);
            target.setTrustScore(clamped);
            // 점수 올린다고 자동으로 밴 해제는 하지 않음(정책 없으므로)
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

    // 내부 로직
    private void autoRecoverIfNeeded(User user) {
        if (user.getStatus() != UserStatus.BANNED)
            return;

        LocalDateTime release = user.getBanReleaseDate();
        if (release == null)
            return; // 영구정지면 null 유지

        // now >= release 면 해제
        if (!LocalDateTime.now().isBefore(release)) {
            user.setStatus(UserStatus.ACTIVE);
            user.setTrustScore(RECOVERED_SCORE);
            user.setBanReleaseDate(null);
            user.setTokenRestricted(false);

            // 밴 횟수는 유지(정책: 삼진아웃 누적)
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
            user.setBanReleaseDate(LocalDateTime.now().plusDays(14)); // 2주
        } else if (count == 2) {
            user.setBanReleaseDate(LocalDateTime.now().plusMonths(1)); // 1달
        } else {
            user.setBanReleaseDate(null); // 3회째: 영구정지
        }
    }

    private int safeInt(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }
}