package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.user.service.UserService;
import com.sealog.backend.global.response.CustomResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserAdminController implements UserAdminControllerDocs {

    private final UserService userAdminService;

    /**
     * 회원가입
     * POST /api/admin/user/signup
     * admin
     */
    @Override
    @PostMapping("/signup")
    public ResponseEntity<CustomResponse<Void>> signUp(
            @Valid @RequestBody AuthRequest.SignUpRequest request
    ) {
        userAdminService.signUp(request);
        return ResponseEntity.ok(CustomResponse.success(null, "회원가입이 성공적으로 완료되었습니다."));
    }
}
