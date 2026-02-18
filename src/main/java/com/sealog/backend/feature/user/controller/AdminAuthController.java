package com.sealog.backend.feature.user.controller;

import com.sealog.backend.feature.user.dto.AuthRequest;
import com.sealog.backend.feature.user.service.AuthService;
import com.sealog.backend.global.core.response.CustomResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/admin/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<CustomResponse<Void>> signUp(
            @Valid @RequestBody AuthRequest.SignUpRequest request
    ) {
        authService.signUp(request);
        return ResponseEntity.ok(CustomResponse.success(null, "회원가입이 성공적으로 완료되었습니다."));
    }
}
