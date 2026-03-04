package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.stack.dto.StackResponse;

import java.util.List;

/**
 * PostStack 비즈니스 로직 인터페이스
 *
 * - 게시글-스택 매핑 관리 (0~5개 제한)
 * - 게시글 수 기반 스택 통계 조회
 */
public interface PostStackService {

    /**
     * 게시글 스택 전체 교체
     * - 기존 매핑 삭제 후 신규 저장
     * - 스택 수 0~5개 제한 검증
     *
     * @param postId   게시글 ID
     * @param stackIds 연결할 스택 ID 목록 (순서 = sortOrder)
     */
    void updatePostStacks(Long postId, List<Long> stackIds);

    /**
     * 게시글에 연결된 스택 이름 목록 조회
     *
     * @param postId 게시글 ID
     * @return 스택 이름 목록 (sortOrder 순)
     */
    List<String> getStackNamesByPostId(Long postId);

    /**
     * 게시글에 연결된 스택 ID 목록 조회 (관련 게시글 추천용)
     *
     * @param postId 게시글 ID
     * @return 스택 ID 목록
     */
    List<Long> getStackIdsByPostId(Long postId);

    /**
     * 게시글에 연결된 스택 매핑 전체 삭제 (스케줄러용)
     *
     * @param postId 게시글 ID
     */
    void deleteAllByPostId(Long postId);

    /**
     * 그룹별 스택 + 공개 게시글 수 조회 (전체)
     */
    StackResponse.GroupedStacks getGroupedStacksWithPostCount();

    /**
     * 그룹별 스택 + 공개 게시글 수 조회 (사용자별)
     *
     * @param nickname 사용자 닉네임
     */
    StackResponse.GroupedStacks getGroupedStacksWithPostCountByUser(String nickname);

    /**
     * 인기 스택 조회 (공개 게시글 기준)
     *
     * @param limit 조회할 스택 수
     */
    List<StackResponse.PopularStack> getPopularStacks(int limit);
}
