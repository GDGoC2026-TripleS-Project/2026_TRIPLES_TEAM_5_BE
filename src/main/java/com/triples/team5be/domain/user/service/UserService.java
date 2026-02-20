package com.triples.team5be.domain.user.service;

import com.triples.team5be.domain.user.dto.LoginRequest;
import com.triples.team5be.domain.user.dto.LoginResponse;
import com.triples.team5be.domain.user.dto.SignUpRequest;
import com.triples.team5be.domain.user.dto.SignUpResponse;
import com.triples.team5be.domain.user.entity.LogoutToken;
import com.triples.team5be.domain.user.entity.TokenBalance;
import com.triples.team5be.domain.user.entity.User;
import com.triples.team5be.domain.user.repository.LogoutTokenRepository;
import com.triples.team5be.domain.user.repository.UserRepository;
import com.triples.team5be.global.auth.JwtTokenProvider;
import com.triples.team5be.global.error.BusinessException;
import com.triples.team5be.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LogoutTokenRepository logoutTokenRepository;

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

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 잘못되었습니다.");
        }

        String token = jwtTokenProvider.createToken(user, user.getRole().name());

        // 로그인 성공
        return new LoginResponse(
                user.getId(),
                user.getUserName(),
                user.getRole().name(),
                token
        );
    }

    @Transactional
    public void logout(String token) {

        // 토큰에서 만료 시간 추출
        java.util.Date expirationDate = jwtTokenProvider.getExpirationDate(token);

        // Date를 LocalDateTime으로 변환
        java.time.LocalDateTime expiry = expirationDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();

        // 저장
        logoutTokenRepository.save(new LogoutToken(token, expiry));
    }

}
