package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackRequest;
import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.domain.feature.stack.service.StackService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 스택 관리 컨트롤러 (관리자 전용)
 *
 * 관리자만 접근 가능한 스택 CRUD API
 * - 스택 생성, 수정, 삭제
 */
@RestController
@RequestMapping("/api/admin/stacks")
@RequiredArgsConstructor
public class StackAdminController implements StackAdminControllerDocs {

    private final StackService stackService;

    /**
     * 전체 스택 목록 조회 / 검색 (페이지네이션)
     * GET /api/admin/stacks?keyword=java&page=0&size=20&sort=name,asc
     */
    @Override
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<StackResponse.StackItem>>> getAll(
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<StackResponse.StackItem> stacks = stackService.getAllStacks(keyword, pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(stacks), "스택 목록 조회 성공"));
    }

    /**
     * 스택 생성
     * POST /api/admin/stacks
     */
    @Override
    @PostMapping
    public ResponseEntity<CustomResponse<StackResponse.StackItem>> create(
            @Valid @RequestBody StackRequest.Create request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StackResponse.StackItem stackItem = stackService.createStack(request, userDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomResponse.success(stackItem, "스택이 생성되었습니다"));
    }

    /**
     * 스택 수정
     * PUT /api/admin/stacks/{stackId}
     *
     * @param stackId 수정할 스택 ID
     */
    @Override
    @PutMapping("/{stackId}")
    public ResponseEntity<CustomResponse<StackResponse.StackItem>> update(
            @PathVariable Long stackId,
            @Valid @RequestBody StackRequest.Update request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StackResponse.StackItem stackItem = stackService.updateStack(stackId, request, userDetails.getUserId());
        return ResponseEntity.ok(CustomResponse.success(stackItem, "스택이 수정되었습니다"));
    }

    /**
     * 스택 삭제
     * DELETE /api/admin/stacks/{stackId}
     * 응답: 204 No Content
     *
     * @param stackId 삭제할 스택 ID
     */
    @Override
    @DeleteMapping("/{stackId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long stackId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        stackService.deleteStack(stackId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
