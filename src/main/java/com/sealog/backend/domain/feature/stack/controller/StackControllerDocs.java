package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.global.response.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Stack", description = "스택 조회 API (인증 불필요)")
public interface StackControllerDocs {

    @Operation(summary = "그룹별 스택 목록 조회 (사용자별)", description = "특정 사용자가 사용 중인 스택을 그룹별로 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 없음",
            content = @Content(schema = @Schema(hidden = true))),
    })
    StackResponse.GroupedStacks getStacksByUser(
        @Parameter(description = "사용자 닉네임", example = "테스터") String nickname
    );

    @Operation(summary = "스택 자동완성 검색", description = "키워드로 스택을 검색합니다. 최대 5개를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    List<StackResponse.StackItem> searchStackByName(
        @Parameter(description = "검색 키워드", example = "spring") String keyword
    );
}
