package com.triples.team5be.domain.user.repository;

import com.triples.team5be.domain.user.entity.LogoutToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogoutTokenRepository extends JpaRepository<LogoutToken, Long> {
    boolean existsByToken(String token);
}
