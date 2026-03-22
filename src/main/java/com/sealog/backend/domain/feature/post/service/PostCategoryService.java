package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.post.entity.PostCategory;
import com.sealog.backend.domain.feature.post.dto.PostResponse;

import java.util.List;

/**
 * PostCategory 비즈니스 로직 인터페이스
 */
public interface PostCategoryService {

    /**
     * 게시글 카테고리 매핑 정보 저장 (전체 교체)
     */
    void savePostCategories(Long postId, List<Long> categoryIds);

    /**
     * 게시글에 연결된 카테고물 엔티티 목록 조회
     */
    List<PostCategory> getPostCategoriesByPostId(Long postId);

    /**
     * 게시글에 연결된 카테고리 요약 정보 목록 조회 (PostResponse용)
     */
    List<PostResponse.CategoryItem> getCategoryItemsByPostId(Long postId);

    /**
     * 특정 게시글의 모든 카테고리 매핑 정보 삭제
     */
    void deleteAllByPostId(Long postId);
}
