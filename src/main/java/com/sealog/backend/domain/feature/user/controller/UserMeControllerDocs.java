package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.base.validation.annotation.CheckFile;
import com.sealog.backend.domain.base.validation.enums.AllowedFileType;
import com.sealog.backend.domain.feature.user.dto.UserMeRequest;
import com.sealog.backend.domain.feature.user.dto.UserMeResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.global.response.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Me", description = "내 정보 API")
@SecurityRequirement(name = "bearerAuth")
public interface UserMeControllerDocs {

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    UserMeResponse.MyProfile getMyInfo(CustomUserDetails userDetails);

    @Operation(summary = "프로필 수정", description = "닉네임/포지션/소개/프로필 이미지 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    UserMeResponse.MyProfile updateProfile(
            CustomUserDetails userDetails,
            UserMeRequest.UpdateProfile request,
            @CheckFile(allowed = {AllowedFileType.IMAGE}, maxSizeMB = 5, nullable = true)
            MultipartFile profileImage
    );

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    CustomResponse<Void> changePassword(
            CustomUserDetails userDetails,
            UserMeRequest.UpdatePassword request
    );
}
