package com.sealog.backend.feature.stack.controller;

import com.sealog.backend.feature.stack.controller.AdminStackControllerDocs;
import com.sealog.backend.feature.stack.dto.StackRequest;
import com.sealog.backend.feature.stack.dto.StackResponse;
import com.sealog.backend.feature.stack.service.AdminStackService;
import com.sealog.backend.global.core.response.CustomResponse;
import com.sealog.backend.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stacks")
@RequiredArgsConstructor
public class AdminStackController implements AdminStackControllerDocs {

    private final AdminStackService adminStackService;

    @Override
    @PostMapping
    public ResponseEntity<CustomResponse<StackResponse.StackItem>> createStack(
            @Valid @RequestBody StackRequest.Create request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StackResponse.StackItem stackItem = adminStackService.createStack(request, userDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomResponse.success(stackItem, "스택이 생성되었습니다"));
    }

    @Override
    @PutMapping("/{stackId}")
    public ResponseEntity<CustomResponse<StackResponse.StackItem>> updateStack(
            @PathVariable Long stackId,
            @Valid @RequestBody StackRequest.Update request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StackResponse.StackItem stackItem = adminStackService.updateStack(stackId, request, userDetails.getUserId());
        return ResponseEntity.ok(CustomResponse.success(stackItem, "스택이 수정되었습니다"));
    }

    @Override
    @DeleteMapping("/{stackId}")
    public ResponseEntity<CustomResponse<Void>> deleteStack(
            @PathVariable Long stackId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        adminStackService.deleteStack(stackId, userDetails.getUserId());
        return ResponseEntity.ok(CustomResponse.success(null, "스택이 삭제되었습니다"));
    }
}
