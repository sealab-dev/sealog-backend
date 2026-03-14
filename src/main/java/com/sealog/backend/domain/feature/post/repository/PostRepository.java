package com.sealog.backend.domain.feature.post.repository;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    // ========== 존재 여부 확인 ========== //

    /**
     * 같은 유저 내 동일 제목 존재 여부 확인 (생성/수정 중복 체크용)
     */
    boolean existsByUserIdAndTitle(Long userId, String title);

    /**
     * 같은 유저 내 동일 slug 존재 여부 확인 (slug 생성 중복 체크용)
     */
    @Query("SELECT COUNT(p) > 0 FROM Post p WHERE p.user.id = :userId AND p.slug = :slug")
    boolean existsByUserIdAndSlug(@Param("userId") Long userId, @Param("slug") String slug);

    /**
     * 같은 유저 내 제목으로 게시글 조회 (수정 시 중복 체크용)
     */
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.title = :title")
    Optional<Post> findByUserIdAndTitle(@Param("userId") Long userId, @Param("title") String title);

    // ========== 공개 게시글 조회 ========== //

    /**
     * 전체 공개 게시글 목록 조회
     * - PUBLISHED 상태 + 삭제되지 않은 게시글
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "WHERE p.status = 'PUBLISHED' " +
            "AND p.deletedAt IS NULL")
    Page<Post> findAllPublished(Pageable pageable);

    /**
     * 특정 유저의 공개 게시글 목록 조회
     * - PUBLISHED 상태 + 삭제되지 않은 게시글
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user u " +
            "WHERE u.nickname = :nickname " +
            "AND p.status = 'PUBLISHED' " +
            "AND p.deletedAt IS NULL")
    Page<Post> findPublishedByNickname(@Param("nickname") String nickname, Pageable pageable);

    /**
     * 닉네임 + slug로 공개 게시글 상세 조회
     * - PUBLISHED 상태 + 삭제되지 않은 게시글
     * - series는 nullable이므로 LEFT JOIN FETCH 사용
     */
    @Query("""
        SELECT p
        FROM Post p
        JOIN FETCH p.user u
        LEFT JOIN FETCH p.series
        WHERE u.nickname = :nickname
          AND p.slug = :slug
          AND p.status = 'PUBLISHED'
          AND p.deletedAt IS NULL
    """)
    Optional<Post> findPublishedByNicknameAndSlug(@Param("nickname") String nickname, @Param("slug") String slug);

    // ========== 내 게시글 조회 (인증) ========== //

    /**
     * 수정할 게시글 조회
     */
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.slug = :slug AND p.deletedAt IS NULL")
    Optional<Post> findByUserIdAndSlug(@Param("userId") Long userId, @Param("slug") String slug);

    /**
     * 삭제된 게시글 목록 조회
     * - deletedAt IS NOT NULL인 게시글만 조회
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "WHERE p.user.id = :userId " +
            "AND p.deletedAt IS NOT NULL")
    Page<Post> findDeletedPostsByUserId(@Param("userId") Long userId, Pageable pageable);


    // ========== 스택 기반 조회 ========== //

    /**
     * 닉네임 + 스택 이름으로 특정 유저의 PUBLISHED 게시글 목록 조회
     * 조인으로만 작성 시, DISTINCT가 필요하므로 페이징 쿼리 불가능
     */
    @Query("""
        SELECT p
        FROM Post p
        JOIN FETCH p.user u
        WHERE u.nickname = :nickname
          AND p.status = 'PUBLISHED'
          AND p.deletedAt IS NULL
          AND EXISTS (
              SELECT 1
              FROM PostStack ps
              WHERE ps.post = p
                AND ps.stack.name = :stackName
          )
    """)
    Page<Post> findPublishedByNicknameAndStackName(@Param("nickname") String nickname, @Param("stackName") String stackName, Pageable pageable);

    // ========== 시리즈 기반 조회 ========== //

    /**
     * 특정 시리즈에 속하는 목록 조회 (특정 회원이 가지는 목록)
     */
    Page<Post> findByUserIdAndSeriesId(
            @Param("userId") Long userId,
            @Param("seriesId") Long seriesId,
            Pageable pageable
    );

    /**
     * 특정 시리즈에 속하는 목록 조회 (상태 값 기준)
     */
    Page<Post> findBySeriesIdAndStatus(
            @Param("seriesId") Long seriesId,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    // ========== 스케줄러용: 영구 삭제 대상 조회 ========== //

    /**
     * N일 이상 경과한 삭제된 게시글 조회 (스케줄러용)
     *
     * @param deletedBefore 이 시각 이전에 삭제된 게시글
     * @return 영구 삭제 대상 게시글 목록
     */
    @Query("SELECT p FROM Post p " +
            "WHERE p.deletedAt IS NOT NULL " +
            "AND p.deletedAt < :deletedBefore")
    List<Post> findPostsToHardDelete(@Param("deletedBefore") LocalDateTime deletedBefore);
}
