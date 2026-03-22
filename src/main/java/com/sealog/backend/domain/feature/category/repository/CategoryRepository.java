package com.sealog.backend.domain.feature.category.repository;

import com.sealog.backend.domain.feature.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 엔티티 리포지토리
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 카테고리 이름으로 단건 조회 (정확히 일치)
     */
    Optional<Category> findByName(String name);

    /**
     * 동일한 이름의 카테고리 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 키워드를 포함하는 카테고리 목록을 대소문자 구분 없이 이름순으로 조회 (자동완성용)
     */
    List<Category> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword);

    /**
     * 키워드를 포함하는 카테고리 목록을 페이징 조회
     */
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
