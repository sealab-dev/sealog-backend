package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackAdminResponse;
import com.sealog.backend.domain.feature.stack.dto.StackAdminRequest;
import com.sealog.backend.domain.feature.stack.service.StackService;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Stack (Admin)", description = "기술 스택 관리 API (관리자 전용)")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/admin/stacks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StackAdminController {

    private final StackService stackService;

    @Operation(summary = "전체 기술 스택 목록 조회 (페이징)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<StackAdminResponse.StackItem> getAll(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<StackAdminResponse.StackItem> results = stackService.getAllStacks(keyword, pageable);
        return PageResponse.from(results);
    }

    @Operation(summary = "신규 기술 스택 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StackAdminResponse.StackItem create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StackAdminRequest.Create request
    ) {
        return stackService.createStack(request, userDetails.getUserId());
    }

    @Operation(summary = "기술 스택 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
    })
    @PutMapping("/{stackId}")
    @ResponseStatus(HttpStatus.OK)
    public StackAdminResponse.StackItem update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long stackId,
            @Valid @RequestBody StackAdminRequest.Update request
    ) {
        return stackService.updateStack(stackId, request, userDetails.getUserId());
    }
}
