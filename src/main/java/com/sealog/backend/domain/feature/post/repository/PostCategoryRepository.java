package com.sealog.backend.domain.feature.post.repository;

import com.sealog.backend.domain.feature.post.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {

    /**
     * 게시글에 연결된 카테고리 매핑 전체 조회 (sortOrder 순)
     */
    List<PostCategory> findAllByPostIdOrderBySortOrderAsc(Long postId);

    /**
     * 게시글에 연결된 카테고리 매핑 전체 삭제
     * - 게시글 삭제(스케줄러) 시 사용
     */
    void deleteAllByPostId(Long postId);

    /**
     * 카테고리에 연결된 매핑 전체 삭제
     * - 어드민의 카테고리 삭제 시 사용
     */
    void deleteAllByCategoryId(Long categoryId);

    /**
     * 게시글에 연결된 카테고리 수 조회
     * - 최대 5개 제한 검증용
     */
    int countByPostId(Long postId);

    /**
     * 공개 게시글에 사용된 카테고리 목록 조회 (게시글 수 포함)
     * - 사이드바 카테고리 클라우드용
     * - 게시글 수 기준 내림차순 정렬
     *
     * @return 카테고리별 공개 게시글 수 (Object[]: Category, count)
     */
    @Query("SELECT pc.category, COUNT(pc.post) as postCount " +
            "FROM PostCategory pc " +
            "WHERE pc.post.status = 'PUBLISHED' " +
            "GROUP BY pc.category " +
            "ORDER BY postCount DESC")
    List<Object[]> findCategoriesWithPublicPostCount();

    /**
     * 특정 사용자의 공개 게시글에 사용된 카테고리 목록 조회 (게시글 수 포함)
     *
     * @param nickname 사용자 닉네임
     * @return 카테고리별 해당 사용자의 공개 게시글 수 (Object[]: Category, count)
     */
    @Query("SELECT pc.category, COUNT(pc.post) as postCount " +
            "FROM PostCategory pc " +
            "WHERE pc.post.status = 'PUBLISHED' " +
            "AND pc.post.user.nickname = :nickname " +
            "GROUP BY pc.category " +
            "ORDER BY postCount DESC")
    List<Object[]> findCategoriesWithPublicPostCountByUser(@Param("nickname") String nickname);

    /**
     * 인기 카테고리 조회 (상위 N개)
     * - 공개 게시글 기준
     *
     * @param limit 조회할 카테고리 수
     * @return 인기 카테고리 목록 (Object[]: Category, count)
     */
    @Query("SELECT pc.category, COUNT(pc.post) as postCount " +
            "FROM PostCategory pc " +
            "WHERE pc.post.status = 'PUBLISHED' " +
            "GROUP BY pc.category " +
            "ORDER BY postCount DESC " +
            "LIMIT :limit")
    List<Object[]> findPopularCategories(@Param("limit") int limit);
}
