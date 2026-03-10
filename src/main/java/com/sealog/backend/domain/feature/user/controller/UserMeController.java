package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserRequest;
import com.sealog.backend.domain.feature.user.service.UserService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.domain.feature.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user/me")
@RequiredArgsConstructor
public class UserMeController implements UserMeControllerDocs {

    private final UserService userService;

    /**
     * 내 정보 조회
     * GET /api/user/me
     * user
     */
    @Override
    @GetMapping
    public ResponseEntity<CustomResponse<UserResponse.MyProfile>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponse.MyProfile response = userService.getMyInfo(userDetails.getUserId());
        return ResponseEntity.ok(CustomResponse.success(response));
    }

    /**
     * 프로필 수정
     * PATCH /api/user/me/profile
     * - 닉네임 및/또는 프로필 이미지 수정
     * - MultipartFile과 JSON을 함께 전송하기 위해 @RequestPart 사용
     * user
     */
    @Override
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<UserResponse.MyProfile>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") @Valid UserRequest.UpdateProfileRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        UserResponse.MyProfile response = userService.updateProfile(
                userDetails.getUserId(),
                request,
                profileImage
        );

        return ResponseEntity.ok(CustomResponse.success(response, "프로필이 수정되었습니다"));
    }

    /**
     * 비밀번호 변경
     * PATCH /api/user/me/password
     * - 현재 비밀번호 확인 후 새 비밀번호로 변경
     * user
     */
    @Override
    @PatchMapping("/password")
    public ResponseEntity<CustomResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserRequest.ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(CustomResponse.success(null, "비밀번호가 변경되었습니다"));
    }
}