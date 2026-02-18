package com.sealog.backend.feature.stack.controller;

import com.sealog.backend.feature.stack.dto.StackRequest;
import com.sealog.backend.feature.stack.dto.StackResponse;
import com.sealog.backend.global.core.response.CustomResponse;
import com.sealog.backend.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin - Stack", description = "스택 관리 API (어드민 전용)")
@SecurityRequirement(name = "bearerAuth")
public interface AdminStackControllerDocs {

    @Operation(summary = "스택 생성", description = "새로운 스택을 생성합니다. 어드민 권한이 필요합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (어드민 전용)",
            content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<StackResponse.StackItem>> createStack(
        StackRequest.Create request,
        CustomUserDetails userDetails
    );

    @Operation(summary = "스택 수정", description = "스택 정보를 수정합니다. 어드민 권한이 필요합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (어드민 전용)",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "스택 없음",
            content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<StackResponse.StackItem>> updateStack(
        @Parameter(description = "스택 ID", example = "1") Long stackId,
        StackRequest.Update request,
        CustomUserDetails userDetails
    );

    @Operation(summary = "스택 삭제", description = "스택을 삭제합니다. 어드민 권한이 필요합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (어드민 전용)",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "스택 없음",
            content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<Void>> deleteStack(
        @Parameter(description = "스택 ID", example = "1") Long stackId,
        CustomUserDetails userDetails
    );
}
