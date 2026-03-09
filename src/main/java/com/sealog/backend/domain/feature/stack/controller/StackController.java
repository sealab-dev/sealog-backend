package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.domain.feature.stack.service.StackService;
import com.sealog.backend.global.response.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest/stacks")
@RequiredArgsConstructor
public class StackController implements StackControllerDocs {

    private final StackService stackService;

    @Override
    @GetMapping("/grouped/user/{nickname}")
    public ResponseEntity<CustomResponse<StackResponse.GroupedStacks>> getGroupedStacksByUser(
            @PathVariable String nickname
    ) {
        return ResponseEntity.ok(CustomResponse.success(stackService.getGroupedStacksWithPostCountByUser(nickname)));
    }

    @Override
    @GetMapping("/autocomplete")
    public ResponseEntity<CustomResponse<List<StackResponse.StackItem>>> autocomplete(
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        return ResponseEntity.ok(CustomResponse.success(stackService.autocomplete(keyword)));
    }
}
