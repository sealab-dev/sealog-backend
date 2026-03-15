package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.base.validation.annotation.CheckFile;
import com.sealog.backend.domain.base.validation.enums.AllowedFileType;
import com.sealog.backend.domain.feature.user.dto.UserMeRequest;
import com.sealog.backend.domain.feature.user.service.UserService;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.domain.feature.user.dto.UserMeResponse;
import com.sealog.backend.global.response.CustomResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserMeController implements UserMeControllerDocs {

    private final UserService userService;

    /**
     * 내 프로필 조회
     * GET /api/me/profile
     * user
     */
    @Override
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
    @Override
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserMeResponse.MyProfile updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") @Valid UserMeRequest.UpdateProfile request,
            @CheckFile(allowed = {AllowedFileType.IMAGE}, maxSizeMB = 5, nullable = true)
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
    @Override
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
