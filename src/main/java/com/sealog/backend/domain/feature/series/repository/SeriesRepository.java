package com.sealog.backend.domain.feature.series.repository;

import com.sealog.backend.domain.feature.series.entity.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


/**
 * Series Entity Spring Data JPA 인터페이스
 */
public interface SeriesRepository extends JpaRepository<Series, Long> {

    // ========== 존재 여부 확인 ========== //

    /**
     * 특정 사용자 기준 특정 이름을 가진 아카이브 존재 확인
     */
    @Query("""
        SELECT COUNT(a) > 0
        FROM Series a
        WHERE a.name = :name AND a.user.id = :userId
    """)
    boolean existsByNameAndUserId(
            @Param("name") String name,
            @Param("userId") Long userId
    );

    // ========== 목록 조회 ========== //

    /**
     * 특정 회원이 가진 아카이브 페이징 목록 조회
     */
    Page<Series> findByUserId(Long userId, Pageable pageable);


    /**
     * 특정 사용자(nickname)의 공개 아카이브 페이징 목록 조회
     */
    @Query("""
        SELECT a
        FROM Series a
        WHERE a.user.nickname = :nickname AND a.isPublic = :isPublic
    """)
    Page<Series> findByUserNicknameAndIsPublic(
            @Param("nickname") String nickname,
            @Param("isPublic") boolean isPublic,
            Pageable pageable
    );


    /**
     * nickname + slug 기준 아카이브 단건 조회
     */
    @Query("""
        SELECT a
        FROM Series a
        LEFT JOIN a.user u
        WHERE a.user.nickname = :nickname AND a.slug = :slug
    """)
    Optional<Series> findByNicknameAndSlug(
            @Param("nickname") String nickname,
            @Param("slug") String slug
    );
}