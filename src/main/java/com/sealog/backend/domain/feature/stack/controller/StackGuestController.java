package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.domain.feature.stack.service.StackGuestService;
import com.sealog.backend.global.response.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest/stack")
@RequiredArgsConstructor
public class StackGuestController implements StackGuestControllerDocs {

    private final StackGuestService stackGuestService;

    @Override
    @GetMapping
    public ResponseEntity<CustomResponse<List<StackResponse.StackItem>>> getAll() {
        return ResponseEntity.ok(CustomResponse.success(stackGuestService.getAllStacks()));
    }

    @Override
    @GetMapping("/grouped")
    public ResponseEntity<CustomResponse<StackResponse.GroupedStacks>> getGrouped() {
        return ResponseEntity.ok(CustomResponse.success(stackGuestService.getGroupedStacksWithPostCount()));
    }

    @Override
    @GetMapping("/grouped/user/{nickname}")
    public ResponseEntity<CustomResponse<StackResponse.GroupedStacks>> getGroupedStacksByUser(
            @PathVariable String nickname
    ) {
        return ResponseEntity.ok(CustomResponse.success(stackGuestService.getGroupedStacksWithPostCountByUser(nickname)));
    }

    @Override
    @GetMapping("/popular")
    public ResponseEntity<CustomResponse<List<StackResponse.PopularStack>>> getPopularStacks(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(CustomResponse.success(stackGuestService.getPopularStacks(limit)));
    }

    @Override
    @GetMapping("/autocomplete")
    public ResponseEntity<CustomResponse<List<StackResponse.StackItem>>> autocomplete(
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        return ResponseEntity.ok(CustomResponse.success(stackGuestService.autocomplete(keyword)));
    }
}
