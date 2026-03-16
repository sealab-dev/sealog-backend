package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.service.UserService;
import com.sealog.backend.global.response.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;

    /**
     * 특정 사용자 프로필 정보 조회
     * GET /api/users/{nickname}/profile
     */
    @Override
    @GetMapping("/{nickname}/profile")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse.UserProfile getBlogUserInfo(
            @PathVariable String nickname
    ) {
        return userService.getPublicProfile(nickname);
    }
}
