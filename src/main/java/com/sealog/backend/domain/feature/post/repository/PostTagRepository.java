package com.sealog.backend.domain.feature.post.repository;

import com.sealog.backend.domain.feature.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    /**
     * 게시글에 연결된 태그 매핑 전체 조회 (sortOrder 순)
     */
    List<PostTag> findAllByPostIdOrderBySortOrderAsc(Long postId);

    /**
     * 게시글에 연결된 태그 매핑 전체 삭제
     * - 게시글 삭제(스케줄러) 시 사용
     */
    void deleteAllByPostId(Long postId);
}
