package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserAdminRequest;
import com.sealog.backend.domain.feature.user.service.UserService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User-Admin", description = "사용자 관리 API (Admin 전용)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserService userService;

    @Operation(
            summary = "사용자 생성",
            description = """
                    새로운 사용자를 생성합니다. (ADMIN 전용)

                    **Request Body**: `UserAdminCreate` (email, password, name, nickname)
                    - email: 이메일 형식
                    - password: 8~20자
                    - name: 2~20자
                    - nickname: 2~20자
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공 (message: '회원가입이 완료되었습니다.')",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (이메일 형식 오류, 길이 제한 등)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "중복된 이메일 또는 닉네임",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomResponse<Void> createUser(
            @Valid @RequestBody UserAdminRequest.Create request
    ) {
        userService.createUser(request);
        return CustomResponse.success("회원가입이 완료되었습니다.");
    }
}
