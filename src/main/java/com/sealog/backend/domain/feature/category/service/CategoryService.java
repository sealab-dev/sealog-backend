package com.sealog.backend.domain.feature.category.service;

import com.sealog.backend.domain.feature.category.dto.CategoryAdminResponse;
import com.sealog.backend.domain.feature.category.dto.CategoryAdminRequest;
import com.sealog.backend.domain.feature.category.dto.CategoryResponse;
import com.sealog.backend.global.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 카테고리 도메인 비즈니스 규약 인터페이스
 */
public interface CategoryService {

    /**
     * 카테고리 자동완성 조회
     * - 키워드를 포함하는 카테고리 목록을 이름순으로 조회합니다.
     *
     * @param keyword 검색 키워드
     * @return 카테고리 요약 정보 목록
     */
    List<CategoryResponse.CategoryItem> autocomplete(String keyword);

    /**
     * 특정 사용자가 사용 중인 카테고리 목록 조회 (그룹별 게시글 수 포함)
     * - 해당 사용자의 공개된(PUBLISHED) 게시글에 연결된 카테고리만 집계합니다.
     *
     * @param nickname 사용자 닉네임
     * @return 그룹별로 분류된 카테고리 및 게시글 수 정보
     */
    CategoryResponse.GroupedCategories getGroupedCategoriesWithPostCountByUser(String nickname);

    // ================= Admin 기능 ================= //

    /**
     * 전체 카테고리 목록 페이징 조회 (관리자용)
     *
     * @param keyword  검색 키워드 (선택 사항, 이름 기준 필터링)
     * @param pageable 페이징 정보
     * @return 카테고리 목록 페이지 응답
     */
    Page<CategoryAdminResponse.CategoryItem> getAllCategories(String keyword, Pageable pageable);

    /**
     * 새로운 카테고리를 생성합니다 (관리자용).
     *
     * @param request 생성 요청 데이터 (이름, 그룹 키 포함)
     * @param userId  작업을 수행하는 관리자 ID
     * @return 생성된 카테고리 상세 정보
     * @throws CustomException.badRequest 이미 동일한 이름의 카테고리가 존재할 경우 발생
     */
    CategoryAdminResponse.CategoryItem createCategory(CategoryAdminRequest.Create request, Long userId);

    /**
     * 기존 카테고리 정보를 수정합니다 (관리자용).
     *
     * @param categoryId 수정할 카테고리 ID
     * @param request    수정 요청 데이터 (이름, 그룹 키 포함)
     * @param userId     작업을 수행하는 관리자 ID
     * @return 수정된 카테고리 상세 정보
     * @throws CustomException.notFound  수정하려는 카테고리 ID가 존재하지 않을 경우 발생
     * @throws CustomException.badRequest 이름 변경 시 이미 동일한 이름이 존재할 경우 발생
     */
    CategoryAdminResponse.CategoryItem updateCategory(Long categoryId, CategoryAdminRequest.Update request, Long userId);

    /**
     * 카테고리를 삭제합니다 (관리자용).
     * - 해당 카테고리와 게시글 간의 모든 매핑 정보(PostCategory)도 함께 삭제됩니다.
     *
     * @param categoryId 삭제할 카테고리 ID
     * @param userId     작업을 수행하는 관리자 ID
     * @throws CustomException.notFound 삭제하려는 카테고리 ID가 존재하지 않을 경우 발생
     */
    void deleteCategory(Long categoryId, Long userId);
}
