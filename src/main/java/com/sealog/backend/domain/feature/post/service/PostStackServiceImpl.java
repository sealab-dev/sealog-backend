package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.entity.PostStack;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.post.repository.PostStackRepository;
import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.enums.StackGroup;
import com.sealog.backend.domain.feature.stack.repository.StackRepository;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PostStack 비즈니스 로직 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostStackServiceImpl implements PostStackService {

    private static final int MAX_STACKS_PER_POST = 5;

    private final PostRepository postRepository;
    private final PostStackRepository postStackRepository;
    private final StackRepository stackRepository;
    private final UserRepository userRepository;

    /**
     * 게시글 스택 전체 교체
     */
    @Override
    @Transactional
    public void updatePostStacks(Long postId, List<Long> stackIds) {
        if (stackIds == null || stackIds.isEmpty()) {
            postStackRepository.deleteAllByPostId(postId);
            return;
        }

        if (stackIds.size() > MAX_STACKS_PER_POST) {
            throw CustomException.badRequest("스택은 최대 " + MAX_STACKS_PER_POST + "개까지 등록할 수 있습니다");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        List<Stack> stacks = stackRepository.findAllById(stackIds);
        if (stacks.size() != stackIds.size()) {
            throw CustomException.badRequest("존재하지 않는 스택이 포함되어 있습니다");
        }

        // stackIds 순서 기준으로 정렬 (sortOrder 보장)
        Map<Long, Stack> stackMap = stacks.stream()
                .collect(Collectors.toMap(Stack::getId, s -> s));

        postStackRepository.deleteAllByPostId(postId);

        List<PostStack> postStacks = new ArrayList<>();
        for (int i = 0; i < stackIds.size(); i++) {
            postStacks.add(PostStack.builder()
                    .post(post)
                    .stack(stackMap.get(stackIds.get(i)))
                    .sortOrder(i)
                    .build());
        }

        postStackRepository.saveAll(postStacks);
    }

    /**
     * 게시글에 연결된 스택 이름 목록 조회
     */
    @Override
    public List<String> getStackNamesByPostId(Long postId) {
        return postStackRepository.findAllByPostIdOrderBySortOrderAsc(postId).stream()
                .map(ps -> ps.getStack().getName())
                .collect(Collectors.toList());
    }

    /**
     * 게시글에 연결된 스택 ID 목록 조회
     */
    @Override
    public List<Long> getStackIdsByPostId(Long postId) {
        return postStackRepository.findAllByPostIdOrderBySortOrderAsc(postId).stream()
                .map(ps -> ps.getStack().getId())
                .collect(Collectors.toList());
    }

    /**
     * 게시글에 연결된 스택 매핑 전체 삭제 (스케줄러용)
     */
    @Override
    @Transactional
    public void deleteAllByPostId(Long postId) {
        postStackRepository.deleteAllByPostId(postId);
    }

    /**
     * 그룹별 스택 + 공개 게시글 수 조회 (전체)
     */
    @Override
    public StackResponse.GroupedStacks getGroupedStacksWithPostCount() {
        List<Object[]> results = postStackRepository.findStacksWithPublicPostCount();
        List<StackResponse.StackWithCount> stacksWithCount = convertToStackWithCount(results);

        Map<StackGroup, List<StackResponse.StackWithCount>> grouped = stacksWithCount.stream()
                .collect(Collectors.groupingBy(StackResponse.StackWithCount::getStackGroup));

        return StackResponse.GroupedStacks.of(grouped);
    }

    /**
     * 그룹별 스택 + 공개 게시글 수 조회 (사용자별)
     */
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

    /**
     * 인기 스택 조회
     */
    @Override
    public List<StackResponse.PopularStack> getPopularStacks(int limit) {
        List<Object[]> results = postStackRepository.findPopularStacks(limit);

        List<StackResponse.PopularStack> popularStacks = new ArrayList<>();
        int rank = 1;
        for (Object[] result : results) {
            Stack stack = (Stack) result[0];
            Long postCount = (Long) result[1];
            popularStacks.add(StackResponse.PopularStack.of(rank++, stack, postCount));
        }

        return popularStacks;
    }

    // ========== Private Methods ========== //

    private List<StackResponse.StackWithCount> convertToStackWithCount(List<Object[]> results) {
        return results.stream()
                .map(result -> {
                    Stack stack = (Stack) result[0];
                    Long postCount = (Long) result[1];
                    return StackResponse.StackWithCount.of(stack, postCount);
                })
                .collect(Collectors.toList());
    }
}
