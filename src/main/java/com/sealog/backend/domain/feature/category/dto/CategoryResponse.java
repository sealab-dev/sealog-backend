package com.sealog.backend.domain.feature.category.dto;

import com.sealog.backend.domain.feature.category.enums.CategoryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class CategoryResponse {

    @Getter
    @Builder
    public static class CategoryItem {
        private Long id;
        private String name;
        private CategoryGroup categoryGroup;

        public static CategoryItem of(Long id, String name, CategoryGroup categoryGroup) {
            return CategoryItem.builder()
                    .id(id)
                    .name(name)
                    .categoryGroup(categoryGroup)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CategoryWithCount {
        private Long id;
        private String name;
        private CategoryGroup categoryGroup;
        private Long postCount;

        public static CategoryWithCount of(Long id, String name, CategoryGroup categoryGroup, Long postCount) {
            return CategoryWithCount.builder()
                    .id(id)
                    .name(name)
                    .categoryGroup(categoryGroup)
                    .postCount(postCount)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class GroupedCategories {
        @Schema(description = "그룹별 카테고리 목록 (키: LANGUAGE, FRAMEWORK 등)")
        private Map<CategoryGroup, List<CategoryWithCount>> categories;

        public static GroupedCategories of(Map<CategoryGroup, List<CategoryWithCount>> categories) {
            return GroupedCategories.builder()
                    .categories(categories)
                    .build();
        }
    }
}
