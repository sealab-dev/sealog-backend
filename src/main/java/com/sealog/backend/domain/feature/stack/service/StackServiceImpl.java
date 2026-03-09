package com.sealog.backend.domain.feature.stack.service;

import com.sealog.backend.domain.feature.post.repository.PostStackRepository;
import com.sealog.backend.domain.feature.stack.dto.StackRequest;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 공개 스택 서비스 구현체
 */
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
                .map(StackResponse.StackItem::from)
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
    public Page<StackResponse.StackItem> getAllStacks(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return stackRepository.findAll(pageable).map(StackResponse.StackItem::from);
        }
        return stackRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                .map(StackResponse.StackItem::from);
    }

    @Override
    @Transactional
    public StackResponse.StackItem createStack(StackRequest.Create request, Long userId) {
        validateDuplicateName(request.getName());
        StackGroup stackGroup = StackGroup.fromKey(request.getStackGroup());

        Stack stack = Stack.builder()
                .name(request.getName())
                .stackGroup(stackGroup)
                .build();

        Stack savedStack = stackRepository.save(stack);
        return StackResponse.StackItem.from(savedStack);
    }

    @Override
    @Transactional
    public StackResponse.StackItem updateStack(Long stackId, StackRequest.Update request, Long userId) {
        Stack stack = findStackById(stackId);
        StackGroup stackGroup = StackGroup.fromKey(request.getStackGroup());

        if (!stack.getName().equals(request.getName())) {
            validateDuplicateName(request.getName());
        }

        stack.updateName(request.getName());
        if (request.getStackGroup() != null) {
            stack.updateStackGroup(stackGroup);
        }

        return StackResponse.StackItem.from(stack);
    }

    @Override
    @Transactional
    public void deleteStack(Long stackId, Long userId) {
        Stack stack = findStackById(stackId);
        postStackRepository.deleteAllByStackId(stackId);
        stackRepository.delete(stack);
    }

    // ========== Private Methods ========== //
    /**
     * DTO
     */
    private List<StackResponse.StackWithCount> convertToStackWithCount(List<Object[]> results) {
        return results.stream()
                .map(result -> {
                    Stack stack = (Stack) result[0];
                    Long postCount = (Long) result[1];
                    return StackResponse.StackWithCount.of(stack, postCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 스택 ID로 Stack 엔티티 조회
     */
    private Stack findStackById(Long stackId) {
        return stackRepository.findById(stackId)
                .orElseThrow(() -> CustomException.notFound("스택을 찾을 수 없습니다"));
    }

    /**
     * 스택명 중복 검사
     */
    private void validateDuplicateName(String name) {
        if (stackRepository.existsByName(name)) {
            throw CustomException.conflict("이미 존재하는 스택명입니다");
        }
    }
}
