package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackRequest;
import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.domain.feature.stack.service.StackAdminService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stack")
@RequiredArgsConstructor
public class StackAdminController implements StackAdminControllerDocs {

    private final StackAdminService stackAdminService;

    @Override
    @PostMapping
    public ResponseEntity<CustomResponse<StackResponse.StackItem>> create(
            @Valid @RequestBody StackRequest.Create request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StackResponse.StackItem stackItem = stackAdminService.createStack(request, userDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomResponse.success(stackItem, "스택이 생성되었습니다"));
    }

    @Override
    @PutMapping("/{stackId}")
    public ResponseEntity<CustomResponse<StackResponse.StackItem>> update(
            @PathVariable Long stackId,
            @Valid @RequestBody StackRequest.Update request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StackResponse.StackItem stackItem = stackAdminService.updateStack(stackId, request, userDetails.getUserId());
        return ResponseEntity.ok(CustomResponse.success(stackItem, "스택이 수정되었습니다"));
    }

    @Override
    @DeleteMapping("/{stackId}")
    public ResponseEntity<CustomResponse<Void>> delete(
            @PathVariable Long stackId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        stackAdminService.deleteStack(stackId, userDetails.getUserId());
        return ResponseEntity.ok(CustomResponse.success(null, "스택이 삭제되었습니다"));
    }
}
