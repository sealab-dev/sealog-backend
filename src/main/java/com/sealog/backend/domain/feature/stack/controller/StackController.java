package com.sealog.backend.domain.feature.stack.controller;

import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.domain.feature.stack.service.StackService;
import com.sealog.backend.global.response.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stacks")
@RequiredArgsConstructor
public class StackController implements StackControllerDocs {

    private final StackService stackService;

    @Override
    @GetMapping("/user/{nickname}")
    @ResponseStatus(HttpStatus.OK)
    public StackResponse.GroupedStacks getStacksByUser(@PathVariable String nickname) {
        return stackService.getGroupedStacksWithPostCountByUser(nickname);
    }

    @Override
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<StackResponse.StackItem> searchStackByName(@RequestParam String keyword) {
        return stackService.autocomplete(keyword);
    }
}
