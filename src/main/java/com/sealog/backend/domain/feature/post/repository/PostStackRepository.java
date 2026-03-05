package com.sealog.backend.domain.feature.post.repository;

import com.sealog.backend.domain.feature.post.entity.PostStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostStackRepository extends JpaRepository<PostStack, Long> {

    /**
     * 게시글에 연결된 스택 매핑 전체 조회 (sortOrder 순)
     */
    List<PostStack> findAllByPostIdOrderBySortOrderAsc(Long postId);

    /**
     * 게시글에 연결된 스택 매핑 전체 삭제
     * - 게시글 삭제(스케줄러) 시 사용
     */
    void deleteAllByPostId(Long postId);

    /**
     * 스택에 연결된 매핑 전체 삭제
     * - 어드민의 스택 삭제 시 사용
     */
    void deleteAllByStackId(Long stackId);

    /**
     * 게시글에 연결된 스택 수 조회
     * - 최대 5개 제한 검증용
     */
    int countByPostId(Long postId);

    /**
     * 공개 게시글에 사용된 스택 목록 조회 (게시글 수 포함)
     * - 사이드바 스택 클라우드용
     * - 게시글 수 기준 내림차순 정렬
     *
     * @return 스택별 공개 게시글 수 (Object[]: Stack, count)
     */
    @Query("SELECT ps.stack, COUNT(ps.post) as postCount " +
            "FROM PostStack ps " +
            "WHERE ps.post.status = 'PUBLISHED' " +
            "GROUP BY ps.stack " +
            "ORDER BY postCount DESC")
    List<Object[]> findStacksWithPublicPostCount();

    /**
     * 특정 사용자의 공개 게시글에 사용된 스택 목록 조회 (게시글 수 포함)
     *
     * @param nickname 사용자 닉네임
     * @return 스택별 해당 사용자의 공개 게시글 수 (Object[]: Stack, count)
     */
    @Query("SELECT ps.stack, COUNT(ps.post) as postCount " +
            "FROM PostStack ps " +
            "WHERE ps.post.status = 'PUBLISHED' " +
            "AND ps.post.user.nickname = :nickname " +
            "GROUP BY ps.stack " +
            "ORDER BY postCount DESC")
    List<Object[]> findStacksWithPublicPostCountByUser(@Param("nickname") String nickname);

    /**
     * 인기 스택 조회 (상위 N개)
     * - 공개 게시글 기준
     *
     * @param limit 조회할 스택 수
     * @return 인기 스택 목록 (Object[]: Stack, count)
     */
    @Query("SELECT ps.stack, COUNT(ps.post) as postCount " +
            "FROM PostStack ps " +
            "WHERE ps.post.status = 'PUBLISHED' " +
            "GROUP BY ps.stack " +
            "ORDER BY postCount DESC " +
            "LIMIT :limit")
    List<Object[]> findPopularStacks(@Param("limit") int limit);
}
