package com.sealog.backend.domain.feature.archive.repository;

import com.sealog.backend.domain.feature.archive.entity.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


/**
 * Archive Entity Spring Data JPA 인터페이스
 */
public interface ArchiveRepository extends JpaRepository<Archive, Long> {

    // ========== 존재 여부 확인 ========== //

    /**
     * 특정 사용자 기준 특정 이름을 가진 아카이브 존재 확인
     */
    @Query("""
        SELECT COUNT(a) > 0
        FROM Archive a
        WHERE a.name = :name AND a.user.id = :userId
    """)
    boolean existsByNameAndUserId(String name, Long userId);

    // ========== 목록 조회 ========== //

    /**
     * 특정 회원이 가진 아카이브 페이징 목록 조회
     */
    Page<Archive> findByUserId(Long userId, Pageable pageable);


    /**
     * 공개 상태의 아카이브 페이징 목록 조회
     */
    Page<Archive> findByIsPublic(boolean isPublic, Pageable pageable);


    /**
     * 특정 사용자(nickname)의 공개 아카이브 페이징 목록 조회
     */
    @Query("""
        SELECT a
        FROM Archive a
        WHERE a.user.nickname = :nickname AND a.isPublic = true
    """)
    Page<Archive> findPublicByUserNickname(String nickname, Pageable pageable);


    /**
     * nickname + slug 기준 아카이브 단건 조회
     */
    @Query("""
        SELECT a
        FROM Archive a
        LEFT JOIN a.user u
        WHERE a.user.nickname = :nickname AND a.slug = :slug
    """)
    Optional<Archive> findByNicknameAndSlug(String nickname, String slug);
}