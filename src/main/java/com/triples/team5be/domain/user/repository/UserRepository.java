package com.triples.team5be.domain.user.repository;

import com.triples.team5be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLoginId(String loginId);

    boolean existsByPhoneNumber(String phoneNumber);

    // 로그인 시 아이디로 유저를 찾기 위한 메서드
    Optional<User> findByLoginId(String loginId);

    @Modifying
    @Query("update User u set u.trustScore = u.trustScore + 1 where u.id = :userId")
    int incrementTrustScore(@Param("userId") Long userId);
}