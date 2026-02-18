package com.sealog.backend.feature.user.controller;

import com.sealog.backend.feature.user.dto.UserRequest;
import com.sealog.backend.feature.user.service.UserService;
import com.sealog.backend.global.core.response.CustomResponse;
import com.sealog.backend.global.core.exception.CustomException;
import com.sealog.backend.global.security.auth.CustomUserDetails;
import com.sealog.backend.feature.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회
     * GET /api/me
     */
    @GetMapping
    public ResponseEntity<CustomResponse<UserResponse.UserInfo>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponse.UserInfo response = userService.getMe(userDetails.getUserId());
        return ResponseEntity.ok(CustomResponse.success(response));
    }

    /**
     * 프로필 수정
     * PATCH /api/me/profile
     * - 닉네임 및/또는 프로필 이미지 수정
     * - MultipartFile과 JSON을 함께 전송하기 위해 @RequestPart 사용
     */
    @PatchMapping("/profile")
    public ResponseEntity<CustomResponse<UserResponse.UserInfo>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(required = false) @Valid UserRequest.UpdateProfileRequest request
    ) {
        if ((request == null || request.getNickname() == null || request.getNickname().isBlank())) {
            throw CustomException.badRequest("수정할 정보를 입력해주세요");
        }

        UserResponse.UserInfo response = userService.updateProfile(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.ok(CustomResponse.success(response, "프로필이 수정되었습니다"));
    }

    /**
     * 비밀번호 변경
     * PATCH /api/me/password
     * - 현재 비밀번호 확인 후 새 비밀번호로 변경
     */
    @PatchMapping("/password")
    public ResponseEntity<CustomResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserRequest.ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(CustomResponse.success(null, "비밀번호가 변경되었습니다"));
    }
}