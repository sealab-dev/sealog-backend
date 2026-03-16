package com.sealog.backend.domain.feature.stack.service;

import com.sealog.backend.domain.feature.stack.dto.StackAdminResponse;
import com.sealog.backend.domain.feature.stack.dto.StackAdminRequest;
import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.global.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 스택 서비스 인터페이스
 */
public interface StackService {

    /**
     * 스택 자동완성 검색
     *
     * @param keyword 검색 키워드
     * @return 검색된 스택 목록 (최대 5개)
     */
    List<StackResponse.StackItem> autocomplete(String keyword);

    /**
     * 그룹별 스택 + 공개 게시글 수 조회 (사용자별)
     *
     * @param nickname 사용자 닉네임
     * @return 그룹별 스택 목록
     */
    StackResponse.GroupedStacks getGroupedStacksWithPostCountByUser(String nickname);

    /* ================= 어드민 전용 =================== */
    /**
     * 전체 스택 목록 조회 - 페이지네이션 (어드민 전용)
     * keyword가 있으면 이름 부분 일치 검색, 없으면 전체 조회
     *
     * @param keyword  검색 키워드 (nullable)
     * @param pageable 페이지 정보
     * @return 스택 페이지
     */
    Page<StackAdminResponse.StackItem> getAllStacks(String keyword, Pageable pageable);

    /**
     * 스택 생성 (어드민 전용)
     *
     * @param request 스택 생성 요청 DTO
     * @param userId 요청 사용자 ID (권한 검증용)
     * @return 생성된 스택 정보
     * @throws CustomException 동일한 이름의 스택이 존재하는 경우 (CONFLICT)
     * @throws CustomException 잘못된 스택 그룹일 경우 (BAD_REQUEST)
     * @throws CustomException 어드민 권한이 없는 경우 (FORBIDDEN)
     */
    StackAdminResponse.StackItem createStack(StackAdminRequest.Create request, Long userId);

    /**
     * 스택 수정 (어드민 전용)
     *
     * @param stackId 수정할 스택 ID
     * @param request 스택 수정 요청 DTO
     * @param userId 요청 사용자 ID (권한 검증용)
     * @return 수정된 스택 정보
     * @throws CustomException 스택을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 변경하려는 이름이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 잘못된 스택 그룹일 경우 (BAD_REQUEST)
     * @throws CustomException 어드민 권한이 없는 경우 (FORBIDDEN)
     */
    StackAdminResponse.StackItem updateStack(Long stackId, StackAdminRequest.Update request, Long userId);

    /**
     * 스택 삭제 (어드민 전용)
     *
     * @param stackId 삭제할 스택 ID
     * @param userId 요청 사용자 ID (권한 검증용)
     * @throws CustomException 스택을 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 어드민 권한이 없는 경우 (FORBIDDEN)
     */
    void deleteStack(Long stackId, Long userId);
}
