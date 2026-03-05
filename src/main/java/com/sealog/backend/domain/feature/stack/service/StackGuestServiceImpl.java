package com.sealog.backend.domain.feature.stack.service;

import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.domain.feature.stack.repository.StackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 공개 스택 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StackGuestServiceImpl implements StackGuestService {

    private final StackRepository stackRepository;

    /**
     * 전체 스택 목록 조회 (게시글 작성용)
     */
    @Override
    public List<StackResponse.StackItem> getAllStacks() {
        return stackRepository.findAll().stream()
                .map(StackResponse.StackItem::from)
                .collect(Collectors.toList());
    }

    /**
     * 스택 자동완성 검색
     */
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
}
