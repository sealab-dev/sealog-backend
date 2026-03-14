package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserRequest;
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
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController implements UserAdminControllerDocs {

    private final UserService userService;

    @Override
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> createUser(
            @Valid @RequestBody UserRequest.Create request
    ) {
        userService.createUser(request);
        return ResponseEntity.ok(CustomResponse.success(null, "사용자가 성공적으로 생성되었습니다."));
    }
}
