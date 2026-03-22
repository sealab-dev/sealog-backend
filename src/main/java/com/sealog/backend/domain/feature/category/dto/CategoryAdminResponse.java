package com.sealog.backend.domain.feature.category.dto;

import com.sealog.backend.domain.feature.category.enums.CategoryGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class CategoryAdminResponse {

    @Getter
    @Builder
    @Schema(name = "CategoryAdminItem")
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
}
