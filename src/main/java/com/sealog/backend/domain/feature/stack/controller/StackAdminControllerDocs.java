package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackAdminResponse;
import com.sealog.backend.domain.feature.stack.dto.StackAdminRequest;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "Stack (Admin)", description = "기술 스택 관리 API (관리자 전용)")
@SecurityRequirement(name = "Bearer Authentication")
public interface StackAdminControllerDocs {

    @Operation(summary = "전체 기술 스택 목록 조회 (페이징)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    PageResponse<StackAdminResponse.StackItem> getAll(String keyword, Pageable pageable);

    @Operation(summary = "신규 기술 스택 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
    })
    StackAdminResponse.StackItem create(CustomUserDetails userDetails, @Valid StackAdminRequest.Create request);

    @Operation(summary = "기술 스택 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
    })
    StackAdminResponse.StackItem update(CustomUserDetails userDetails, Long stackId, @Valid StackAdminRequest.Update request);
}
