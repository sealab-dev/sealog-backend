package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserSocialLinkRequest;
import com.sealog.backend.domain.feature.user.dto.UserSocialLinkResponse;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Me - Social Link", description = "내 소셜 링크 API")
@SecurityRequirement(name = "bearerAuth")
public interface UserSocialLinkMeControllerDocs {

    @Operation(summary = "내 소셜 링크 목록 조회", description = "로그인한 사용자의 소셜 링크 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<List<UserSocialLinkResponse.LinkInfo>>> getMyLinks(
            CustomUserDetails userDetails
    );

    @Operation(summary = "소셜 링크 전체 저장", description = "소셜 링크 목록을 전체 upsert합니다. 빈 배열 전송 시 전체 삭제됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<List<UserSocialLinkResponse.LinkInfo>>> upsertLinks(
            CustomUserDetails userDetails,
            UserSocialLinkRequest.UpsertRequest request
    );
}
