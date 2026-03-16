package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.base.validation.annotation.CheckFile;
import com.sealog.backend.domain.base.validation.enums.AllowedFileType;
import com.sealog.backend.domain.feature.user.dto.UserMeRequest;
import com.sealog.backend.domain.feature.user.service.UserService;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.domain.feature.user.dto.UserMeResponse;
import com.sealog.backend.global.response.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Me", description = "내 정보 API")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserMeController {

    private final UserService userService;

    /**
     * 내 프로필 조회
     * GET /api/me/profile
     * user
     */
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public UserMeResponse.MyProfile getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userService.getMyProfile(userDetails.getUserId());
    }

    /**
     * 프로필 수정
     * PATCH /api/me/profile
     * - 닉네임 및/또는 프로필 이미지 수정
     * - MultipartFile과 JSON을 함께 전송하기 위해 @RequestPart 사용
     * user
     */
    @Operation(summary = "프로필 수정", description = "닉네임/포지션/소개/프로필 이미지 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserMeResponse.MyProfile updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") @Valid UserMeRequest.UpdateProfile request,
            @CheckFile(allowed = {AllowedFileType.IMAGE}, maxSizeMB = 10, nullable = true)
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return userService.updateProfile(userDetails.getUserId(), request, profileImage);
    }

    /**
     * 비밀번호 변경
     * PATCH /api/me/password
     * - 현재 비밀번호 확인 후 새 비밀번호로 변경
     * user
     */
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserMeRequest.UpdatePassword request
    ) {
        userService.updatePassword(userDetails.getUserId(), request);
        return CustomResponse.success("비밀번호가 변경되었습니다");
    }
}
