package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.user.dto.UserMeRequest;
import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.dto.UserMeResponse;
import com.sealog.backend.global.exception.CustomException;

import java.util.List;

/**
 * 사용자 소셜 링크 도메인 비즈니스 규약 인터페이스
 */
public interface UserSocialService {

    /**
     * 사용자의 모든 소셜 링크 목록을 조회합니다 (관리용).
     *
     * @param userId 사용자 ID
     * @return 소셜 링크 상세 정보 목록
     */
    List<UserMeResponse.MySocialLinkItem> getMyLinks(Long userId);

    /**
     * 사용자의 소셜 링크 정보를 전체 업데이트합니다.
     * - 기존 링크를 모두 삭제하고 요청된 목록으로 새로 저장합니다.
     *
     * @param userId  사용자 ID
     * @param request 업데이트할 소셜 링크 목록
     * @return 업데이트 완료된 소셜 링크 목록
     * @throws CustomException.notFound  사용자를 찾을 수 없는 경우 발생
     * @throws CustomException.badRequest 동일한 소셜 타입이 중복되어 요청된 경우 발생
     */
    List<UserMeResponse.MySocialLinkItem> update(Long userId, List<UserMeRequest.UpdateSocialLink> request);

    /**
     * 특정 사용자의 소셜 링크 목록을 조회합니다 (공개용).
     *
     * @param nickname 사용자 닉네임
     * @return 공개된 소셜 링크 목록
     * @throws CustomException.notFound 사용자를 찾을 수 없는 경우 발생
     */
    List<UserResponse.SocialLinkItem> getPublicLinks(String nickname);
}
