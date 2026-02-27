package com.sealog.backend.domain.feature.user.repository;

import com.sealog.backend.domain.feature.user.entity.UserSocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSocialLinkRepository extends JpaRepository<UserSocialLink, Long> {

    /**
     * 사용자 ID로 소셜 링크 전체 조회
     *
     * @param userId 사용자 ID
     * @return 소셜 링크 목록
     */
    List<UserSocialLink> findAllByUserId(Long userId);

    /**
     * 닉네임으로 소셜 링크 전체 조회 (게스트용)
     *
     * @param nickname 사용자 닉네임
     * @return 소셜 링크 목록
     */
    List<UserSocialLink> findAllByUser_Nickname(String nickname);

    /**
     * 사용자 ID로 소셜 링크 전체 삭제 (upsert 시 초기화)
     *
     * @param userId 사용자 ID
     */
    @Modifying
    @Query("DELETE FROM UserSocialLink u WHERE u.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
