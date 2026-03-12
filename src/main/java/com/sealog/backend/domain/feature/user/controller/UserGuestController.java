package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.service.UserService;
import com.sealog.backend.global.response.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest/user")
@RequiredArgsConstructor
public class UserGuestController implements UserGuestControllerDocs {

    private final UserService userService;

    /**
     * 특정 사용자 프로필 정보 조회
     * GET /api/guest/user/{nickname}/profile
     */
    @Override
    @GetMapping("/{nickname}/profile")
    public ResponseEntity<CustomResponse<UserResponse.PublicProfile>> getBlogUserInfo(
            @PathVariable String nickname
    ) {
        UserResponse.PublicProfile blogUser = userService.getPublicProfile(nickname);
        return ResponseEntity.ok(CustomResponse.success(blogUser, "정보 조회가 성공적으로 완료되었습니다."));
    }
}
