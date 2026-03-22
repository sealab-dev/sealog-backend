package com.sealog.backend.domain.feature.category.controller;

import com.sealog.backend.domain.feature.category.dto.CategoryResponse;
import com.sealog.backend.domain.feature.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category", description = "카테고리 조회 API (인증 불필요)")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "특정 사용자의 카테고리 목록 조회",
            description = "특정 사용자가 사용 중인 카테고리를 그룹(LANGUAGE / FRAMEWORK 등)별로 게시글 수와 함께 반환합니다. 응답 data: `GroupedCategories`"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CategoryResponse.GroupedCategories.class)))
    @GetMapping("/user/{nickname}")
    public CategoryResponse.GroupedCategories getCategoriesByUser(
            @PathVariable String nickname
    ) {
        return categoryService.getGroupedCategoriesWithPostCountByUser(nickname);
    }

    @Operation(
            summary = "카테고리 자동완성 검색",
            description = "키워드로 카테고리명을 검색합니다. 최대 5개를 반환합니다. 게시글 작성 시 카테고리 선택에 활용합니다. 응답 data: `List<CategoryItem>`"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = CategoryResponse.CategoryItem.class)))
    @GetMapping("/search")
    public List<CategoryResponse.CategoryItem> searchCategoryByName(
            @RequestParam String keyword
    ) {
        return categoryService.autocomplete(keyword);
    }
}
