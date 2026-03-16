package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackAdminResponse;
import com.sealog.backend.domain.feature.stack.dto.StackAdminRequest;
import com.sealog.backend.domain.feature.stack.service.StackService;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

@Tag(name = "Stack (Admin)", description = "기술 스택 관리 API (ADMIN 권한 필요)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/stacks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StackAdminController {

    private final StackService stackService;

    @Operation(
            summary = "전체 기술 스택 목록 조회 (페이징)",
            description = "키워드로 필터링하여 전체 스택 목록을 페이지 조회합니다. 응답 data: `PageResponse<StackItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<StackAdminResponse.StackItem> getAll(
            @Parameter(description = "검색 키워드 (스택명 필터, 미입력 시 전체 조회)", example = "spring")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<StackAdminResponse.StackItem> results = stackService.getAllStacks(keyword, pageable);
        return PageResponse.from(results);
    }

    @Operation(
            summary = "신규 기술 스택 생성",
            description = "새로운 기술 스택을 생성합니다. **Request Body**: `StackCreate` (name, stackGroup). 응답 data: `StackItem`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = StackAdminResponse.StackItem.class))),
            @ApiResponse(responseCode = "400", description = "요청값 오류 (이름 누락, 그룹 누락 등)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StackAdminResponse.StackItem create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StackAdminRequest.Create request
    ) {
        return stackService.createStack(request, userDetails.getUserId());
    }

    @Operation(
            summary = "기술 스택 정보 수정",
            description = "기존 스택의 이름 및 그룹을 수정합니다. **Request Body**: `StackUpdate` (name, stackGroup). 응답 data: `StackItem`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = StackAdminResponse.StackItem.class))),
            @ApiResponse(responseCode = "400", description = "요청값 오류",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "스택 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PutMapping("/{stackId}")
    @ResponseStatus(HttpStatus.OK)
    public StackAdminResponse.StackItem update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "수정할 스택 ID", example = "1") @PathVariable Long stackId,
            @Valid @RequestBody StackAdminRequest.Update request
    ) {
        return stackService.updateStack(stackId, request, userDetails.getUserId());
    }
}
