package com.sealog.backend.domain.feature.category.controller;

import com.sealog.backend.domain.feature.category.dto.CategoryAdminResponse;
import com.sealog.backend.domain.feature.category.dto.CategoryAdminRequest;
import com.sealog.backend.domain.feature.category.service.CategoryService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Category (Admin)", description = "카테고리 관리 API (ADMIN 권한 필요)")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;

    @Operation(
            summary = "전체 카테고리 페이징 조회",
            description = "키워드로 필터링하여 전체 카테고리 목록을 페이지 조회합니다. 응답 data: `PageResponse<CategoryItem>`"
    )
    @GetMapping
    public PageResponse<CategoryAdminResponse.CategoryItem> getAll(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return PageResponse.from(categoryService.getAllCategories(keyword, pageable));
    }

    @Operation(
            summary = "새 카테고리 생성",
            description = "새로운 카테고리를 생성합니다. 응답 data: `CategoryItem`"
    )
    @PostMapping
    public CategoryAdminResponse.CategoryItem create(
            @RequestBody @Valid CategoryAdminRequest.Create request
    ) {
        // TODO: SecurityContext에서 실제 관리자 ID 가져오기 (현재는 임시 1L)
        return categoryService.createCategory(request, 1L);
    }

    @Operation(
            summary = "카테고리 정보 수정",
            description = "기존 카테고리의 이름 또는 그룹을 수정합니다. 응답 data: `CategoryItem`"
    )
    @PutMapping("/{categoryId}")
    public CategoryAdminResponse.CategoryItem update(
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryAdminRequest.Update request
    ) {
        // TODO: SecurityContext에서 실제 관리자 ID 가져오기
        return categoryService.updateCategory(categoryId, request, 1L);
    }

    @Operation(
            summary = "카테고리 삭제",
            description = "카테고리를 삭제합니다. 연결된 게시글 매핑 정보도 함께 삭제됩니다."
    )
    @DeleteMapping("/{categoryId}")
    public CustomResponse<Void> delete(
            @PathVariable Long categoryId
    ) {
        // TODO: SecurityContext에서 실제 관리자 ID 가져오기
        categoryService.deleteCategory(categoryId, 1L);
        return CustomResponse.success("카테고리가 삭제되었습니다.");
    }
}
