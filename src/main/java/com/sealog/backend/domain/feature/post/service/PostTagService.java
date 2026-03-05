package com.sealog.backend.domain.feature.post.service;

import java.util.List;

/**
 * PostTag 비즈니스 로직 인터페이스
 *
 * - 게시글-태그 매핑 관리
 * - 태그 findOrCreate 패턴 내부 처리
 */
public interface PostTagService {

    /**
     * 게시글 태그 전체 교체
     * - 기존 매핑 삭제 후 신규 저장
     * - 태그명 기준 findOrCreate 처리
     *
     * @param postId   게시글 ID
     * @param tagNames 연결할 태그 이름 목록 (순서 = sortOrder)
     */
    void updatePostTags(Long postId, List<String> tagNames);

    /**
     * 게시글에 연결된 태그 이름 목록 조회
     *
     * @param postId 게시글 ID
     * @return 태그 이름 목록 (sortOrder 순)
     */
    List<String> getTagNamesByPostId(Long postId);

    /**
     * 게시글에 연결된 태그 매핑 전체 삭제 (스케줄러용)
     *
     * @param postId 게시글 ID
     */
    void deleteAllByPostId(Long postId);
}
