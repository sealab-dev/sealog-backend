package com.sealog.backend.domain.feature.stack.service;

import com.sealog.backend.domain.feature.stack.dto.StackResponse;

import java.util.List;

/**
 * 공개 스택 서비스 인터페이스
 *
 * 순수 스택 조회 (게시글 작성 시 스택 선택용)
 */
public interface StackGuestService {

    /**
     * 전체 스택 목록 조회
     * - DB에 등록된 모든 스택 반환 (그룹 정보 포함)
     * - 게시글 작성 시 스택 선택 목록으로 사용
     *
     * @return 스택 목록
     */
    List<StackResponse.StackItem> getAllStacks();

    /**
     * 스택 자동완성 검색
     *
     * @param keyword 검색 키워드
     * @return 검색된 스택 목록 (최대 5개)
     */
    List<StackResponse.StackItem> autocomplete(String keyword);
}
