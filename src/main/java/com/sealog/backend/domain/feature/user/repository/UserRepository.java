package com.sealog.backend.domain.feature.user.repository;

import com.sealog.backend.domain.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 엔티티 리포지토리
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 이메일 중복 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 중복 여부 확인
     */
    boolean existsByNickname(String nickname);
}
