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
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user u " +
            "WHERE u.nickname = :nickname " +
            "AND p.slug = :slug " +
            "AND p.status = 'PUBLISHED' " +
            "AND p.deletedAt IS NULL")
    Optional<Post> findPublishedByNicknameAndSlug(@Param("nickname") String nickname, @Param("slug") String slug);

    /**
     * 키워드로 공개 게시글 자동완성 검색
     * - 제목 우선 매칭, PUBLISHED + 삭제되지 않은 게시글
     * - Pageable로 최대 개수 제한 (서비스에서 PageRequest.of(0, 10) 전달)
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "WHERE p.status = 'PUBLISHED' " +
            "AND p.deletedAt IS NULL " +
            "AND (p.title LIKE %:keyword% OR p.excerpt LIKE %:keyword%) " +
            "ORDER BY " +
            "CASE WHEN p.title LIKE %:keyword% THEN 0 ELSE 1 END, " +
            "p.createdAt DESC")
    List<Post> findPublishedByKeyword(@Param("keyword") String keyword, Pageable pageable);

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


    // ========== 아카이브 기반 조회 ========== //

    /**
     * 특정 아카이브에 속하는 목록 조회 (특정 회원이 가지는 목록)
     */
    Page<Post> findByUserIdAndArchiveId(
            @Param("userId") Long userId,
            @Param("archiveId") Long archiveId,
            Pageable pageable
    );

    /**
     * 특정 아카이브에 속하는 목록 조회 (상태 값 기준)
     */
    Page<Post> findByArchiveIdAndStatus(
            @Param("archiveId") Long archiveId,
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
