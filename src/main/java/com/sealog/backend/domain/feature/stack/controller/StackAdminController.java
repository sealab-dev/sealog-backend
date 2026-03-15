package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackAdminResponse;
import com.sealog.backend.domain.feature.stack.dto.StackAdminRequest;
import com.sealog.backend.domain.feature.stack.service.StackService;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stacks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StackAdminController implements StackAdminControllerDocs {

    private final StackService stackService;

    @Override
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<StackAdminResponse.StackItem> getAll(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<StackAdminResponse.StackItem> results = stackService.getAllStacks(keyword, pageable);
        return PageResponse.from(results);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StackAdminResponse.StackItem create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StackAdminRequest.Create request
    ) {
        return stackService.createStack(request, userDetails.getUserId());
    }

    @Override
    @PutMapping("/{stackId}")
    @ResponseStatus(HttpStatus.OK)
    public StackAdminResponse.StackItem update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long stackId,
            @Valid @RequestBody StackAdminRequest.Update request
    ) {
        return stackService.updateStack(stackId, request, userDetails.getUserId());
    }
}
