package com.sealog.backend.domain.feature.category.service;

import com.sealog.backend.domain.feature.post.repository.PostCategoryRepository;
import com.sealog.backend.domain.feature.category.dto.CategoryAdminResponse;
import com.sealog.backend.domain.feature.category.dto.CategoryAdminRequest;
import com.sealog.backend.domain.feature.category.dto.CategoryResponse;
import com.sealog.backend.domain.feature.category.entity.Category;
import com.sealog.backend.domain.feature.category.enums.CategoryGroup;
import com.sealog.backend.domain.feature.category.repository.CategoryRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostCategoryRepository postCategoryRepository;

    @Override
    public List<CategoryResponse.CategoryItem> autocomplete(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return categoryRepository.findByNameContainingIgnoreCaseOrderByNameAsc(keyword.trim()).stream()
                .map(category -> CategoryResponse.CategoryItem.of(category.getId(), category.getName(), category.getCategoryGroup()))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse.GroupedCategories getGroupedCategoriesWithPostCountByUser(String nickname) {
        // 사용자의 공개 게시글에 사용된 카테고리별 게시글 수 조회
        List<Object[]> results = postCategoryRepository.findCategoriesWithPublicPostCountByUser(nickname);
        List<CategoryResponse.CategoryWithCount> categoriesWithCount = convertToCategoryWithCount(results);

        // 그룹별로 그룹화
        Map<CategoryGroup, List<CategoryResponse.CategoryWithCount>> grouped = categoriesWithCount.stream()
                .collect(Collectors.groupingBy(CategoryResponse.CategoryWithCount::getCategoryGroup));

        return CategoryResponse.GroupedCategories.of(grouped);
    }

    @Override
    public Page<CategoryAdminResponse.CategoryItem> getAllCategories(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findAll(pageable)
                    .map(category -> CategoryAdminResponse.CategoryItem.of(category.getId(), category.getName(), category.getCategoryGroup()));
        }
        return categoryRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                .map(category -> CategoryAdminResponse.CategoryItem.of(category.getId(), category.getName(), category.getCategoryGroup()));
    }

    @Override
    @Transactional
    public CategoryAdminResponse.CategoryItem createCategory(CategoryAdminRequest.Create request, Long userId) {
        validateDuplicateName(request.getName());

        CategoryGroup categoryGroup = CategoryGroup.fromKey(request.getCategoryGroup());

        Category category = Category.builder()
                .name(request.getName())
                .categoryGroup(categoryGroup)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return CategoryAdminResponse.CategoryItem.of(savedCategory.getId(), savedCategory.getName(), savedCategory.getCategoryGroup());
    }

    @Override
    @Transactional
    public CategoryAdminResponse.CategoryItem updateCategory(Long categoryId, CategoryAdminRequest.Update request, Long userId) {
        Category category = findCategoryById(categoryId);

        CategoryGroup categoryGroup = CategoryGroup.fromKey(request.getCategoryGroup());

        // 이름이 변경된 경우 중복 체크
        if (!category.getName().equals(request.getName())) {
            validateDuplicateName(request.getName());
        }

        category.updateName(request.getName());
        if (request.getCategoryGroup() != null) {
            category.updateCategoryGroup(categoryGroup);
        }

        return CategoryAdminResponse.CategoryItem.of(category.getId(), category.getName(), category.getCategoryGroup());
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId, Long userId) {
        Category category = findCategoryById(categoryId);
        // 연결된 게시글 매핑 정보 선삭제
        postCategoryRepository.deleteAllByCategoryId(categoryId);
        categoryRepository.delete(category);
    }

    private List<CategoryResponse.CategoryWithCount> convertToCategoryWithCount(List<Object[]> results) {
        return results.stream()
                .map(result -> {
                    Category category = (Category) result[0];
                    Long postCount = (Long) result[1];
                    return CategoryResponse.CategoryWithCount.of(category.getId(), category.getName(), category.getCategoryGroup(), postCount);
                })
                .collect(Collectors.toList());
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> CustomException.notFound("해당 카테고리를 찾을 수 없습니다. ID: " + categoryId));
    }

    private void validateDuplicateName(String name) {
        if (categoryRepository.existsByName(name)) {
            throw CustomException.badRequest("이미 존재하는 카테고리 이름입니다: " + name);
        }
    }
}
