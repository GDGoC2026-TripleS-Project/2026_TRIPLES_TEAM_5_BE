package com.triples.team5be.domain.user.repository;

import com.triples.team5be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByPhoneNumber(String phoneNumber);

    // 로그인 시 아이디로 유저를 찾기 위한 메서드
    Optional<User> findByLoginId(String loginId);
}
