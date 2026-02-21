package com.triples.team5be.global.config;

import com.triples.team5be.global.auth.JwtAuthenticationFilter;
import com.triples.team5be.global.auth.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtTokenProvider jwtTokenProvider;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable()) // API 테스트 위해 잠깐 비활성화
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)

                                // JWT 사용: 세션 안 씀
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // API 접근 권한
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/swagger-ui.html").permitAll()

                                                // 조회수 집계/중복방지 API는 익명도 가능(anonymousId 쿠키 사용)
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/posts/*/views")
                                                .permitAll()

                                                // 그 외는 모두 인증 필요
                                                .anyRequest().authenticated())

                                // Bearer 토큰 인증 필터 추가
                                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                                                UsernamePasswordAuthenticationFilter.class)

                                // 인증/인가 실패 시 상태코드 정리
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((req, res, e) -> res
                                                                .setStatus(HttpServletResponse.SC_UNAUTHORIZED)) // 401
                                                .accessDeniedHandler((req, res, e) -> res
                                                                .setStatus(HttpServletResponse.SC_FORBIDDEN)) // 403
                                );

                return http.build();
        }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://51.20.78.158:8080"
//                "https://yourdomain.com"  도메인 연결 시 수정
        ));

        // 허용할 HTTP 메서드
        configuration.addAllowedMethod("*");

        // 허용할 헤더
        configuration.addAllowedHeader("*");

        // 쿠키 및 인증 헤더 허용
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

