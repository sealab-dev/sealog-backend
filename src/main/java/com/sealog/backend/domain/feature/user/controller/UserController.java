package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "블로그 사용자 정보 API (인증 불필요)")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 특정 사용자 프로필 정보 조회
     * GET /api/users/{nickname}/profile
     */
    @Operation(summary = "블로그 사용자 정보 조회", description = "닉네임으로 블로그 사용자 정보를 조회합니다.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/{nickname}/profile")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse.UserProfile getBlogUserInfo(
            @Parameter(description = "사용자 닉네임", example = "테스터") @PathVariable String nickname
    ) {
        return userService.getPublicProfile(nickname);
    }
}
