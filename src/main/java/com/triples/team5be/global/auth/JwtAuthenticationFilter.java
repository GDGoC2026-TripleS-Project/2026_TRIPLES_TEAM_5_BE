package com.triples.team5be.global.auth;

import com.triples.team5be.domain.user.repository.LogoutTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final LogoutTokenRepository logoutTokenRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, LogoutTokenRepository logoutTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.logoutTokenRepository = logoutTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 토큰 추출
        String token = resolveToken(request);

        // 유효한지 검사
        if (token != null && jwtTokenProvider.validateToken(token)) {

            if (logoutTokenRepository.existsByToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}