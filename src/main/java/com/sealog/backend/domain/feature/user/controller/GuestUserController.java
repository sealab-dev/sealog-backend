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
public class GuestUserController implements GuestUserControllerDocs {

    private final UserService userService;

    /**
     * 블로그 사용자 정보
     * GET /api/guest/user/{nickname}
     * 게스트
     */
    @Override
    @GetMapping("/{nickname}")
    public ResponseEntity<CustomResponse<UserResponse.BlogUserInfo>> getBlogUserInfo(
            @PathVariable String nickname
    ) {
        UserResponse.BlogUserInfo blogUser = userService.getBlogUser(nickname);
        return ResponseEntity.ok(CustomResponse.success(blogUser, "정보 조회가 성공적으로 완료되었습니다."));
    }
}
