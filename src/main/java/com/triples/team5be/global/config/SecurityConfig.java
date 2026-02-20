package com.triples.team5be.global.config;

import com.triples.team5be.domain.user.repository.LogoutTokenRepository;
import com.triples.team5be.global.auth.JwtAuthenticationFilter;
import com.triples.team5be.global.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final LogoutTokenRepository logoutTokenRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보안 비활성화 (REST API 환경)
                .csrf(AbstractHttpConfigurer::disable)
                // 폼 로그인 및 기본 HTTP 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // API 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // auth 관련 경로 모두 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        // Swagger 관련 경로 모두 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()               // 그 외는 모두 인증 필요
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, logoutTokenRepository),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
