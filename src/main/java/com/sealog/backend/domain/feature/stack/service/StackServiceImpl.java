package com.sealog.backend.domain.feature.stack.service;

import com.sealog.backend.domain.feature.post.repository.PostStackRepository;
import com.sealog.backend.domain.feature.stack.dto.StackAdminResponse;
import com.sealog.backend.domain.feature.stack.dto.StackAdminRequest;
import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.enums.StackGroup;
import com.sealog.backend.domain.feature.stack.repository.StackRepository;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StackServiceImpl implements StackService {

    private final StackRepository stackRepository;
    private final PostStackRepository postStackRepository;
    private final UserRepository userRepository;

    @Override
    public List<StackResponse.StackItem> autocomplete(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return stackRepository.findByNameContainingIgnoreCaseOrderByNameAsc(
                        keyword.trim(),
                        PageRequest.of(0, 5)
                ).stream()
                .map(stack -> StackResponse.StackItem.of(stack.getId(), stack.getName(), stack.getStackGroup()))
                .collect(Collectors.toList());
    }

    @Override
    public StackResponse.GroupedStacks getGroupedStacksWithPostCountByUser(String nickname) {
        userRepository.findByNickname(nickname)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        List<Object[]> results = postStackRepository.findStacksWithPublicPostCountByUser(nickname);
        List<StackResponse.StackWithCount> stacksWithCount = convertToStackWithCount(results);

        Map<StackGroup, List<StackResponse.StackWithCount>> grouped = stacksWithCount.stream()
                .collect(Collectors.groupingBy(StackResponse.StackWithCount::getStackGroup));

        return StackResponse.GroupedStacks.of(grouped);
    }

    @Override
    public Page<StackAdminResponse.StackItem> getAllStacks(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return stackRepository.findAll(pageable)
                    .map(stack -> StackAdminResponse.StackItem.of(stack.getId(), stack.getName(), stack.getStackGroup()));
        }
        return stackRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                .map(stack -> StackAdminResponse.StackItem.of(stack.getId(), stack.getName(), stack.getStackGroup()));
    }

    @Override
    @Transactional
    public StackAdminResponse.StackItem createStack(StackAdminRequest.Create request, Long userId) {
        validateDuplicateName(request.getName());
        StackGroup stackGroup = StackGroup.fromKey(request.getStackGroup());

        Stack stack = Stack.builder()
                .name(request.getName())
                .stackGroup(stackGroup)
                .build();

        Stack savedStack = stackRepository.save(stack);
        return StackAdminResponse.StackItem.of(savedStack.getId(), savedStack.getName(), savedStack.getStackGroup());
    }

    @Override
    @Transactional
    public StackAdminResponse.StackItem updateStack(Long stackId, StackAdminRequest.Update request, Long userId) {
        Stack stack = findStackById(stackId);
        StackGroup stackGroup = StackGroup.fromKey(request.getStackGroup());

        if (!stack.getName().equals(request.getName())) {
            validateDuplicateName(request.getName());
        }

        stack.updateName(request.getName());
        if (request.getStackGroup() != null) {
            stack.updateStackGroup(stackGroup);
        }

        return StackAdminResponse.StackItem.of(stack.getId(), stack.getName(), stack.getStackGroup());
    }

    @Override
    @Transactional
    public void deleteStack(Long stackId, Long userId) {
        Stack stack = findStackById(stackId);
        postStackRepository.deleteAllByStackId(stackId);
        stackRepository.delete(stack);
    }

    // ========== Private Methods ========== //
    private List<StackResponse.StackWithCount> convertToStackWithCount(List<Object[]> results) {
        return results.stream()
                .map(result -> {
                    Stack stack = (Stack) result[0];
                    Long postCount = (Long) result[1];
                    return StackResponse.StackWithCount.of(stack.getId(), stack.getName(), stack.getStackGroup(), postCount);
                })
                .collect(Collectors.toList());
    }

    private Stack findStackById(Long stackId) {
        return stackRepository.findById(stackId)
                .orElseThrow(() -> CustomException.notFound("스택을 찾을 수 없습니다"));
    }

    private void validateDuplicateName(String name) {
        if (stackRepository.existsByName(name)) {
            throw CustomException.conflict("이미 존재하는 스택명입니다");
        }
    }
}
