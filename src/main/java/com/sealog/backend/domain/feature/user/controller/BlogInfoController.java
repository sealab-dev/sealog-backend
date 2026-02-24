package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.service.UserService;
import com.sealog.backend.global.response.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class BlogInfoController implements BlogInfoControllerDocs{

    private final UserService userService;

    /**
     * 블로그 사용자 정보
     * GET /api/user/{nickname}
     */
    @Override
    @GetMapping("/{nickname}")
    public ResponseEntity<CustomResponse<UserResponse.BlogUserInfo>> signUp(
            @PathVariable String nickname
    ) {
        UserResponse.BlogUserInfo blogUser = userService.getBlogUser(nickname);
        return ResponseEntity.ok(CustomResponse.success(blogUser, "회원가입이 성공적으로 완료되었습니다."));
    }
}
