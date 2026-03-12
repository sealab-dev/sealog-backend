package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.user.dto.UserRequest;
import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.global.exception.CustomException;

import java.util.List;

public interface UserSocialService {

    /**
     * 내 소셜 링크 목록 조회
     *
     * @param userId 사용자 ID
     * @return 소셜 링크 목록
     */
    List<UserResponse.SocialLinkItem> getMyLinks(Long userId);

    /**
     * 소셜 링크 전체 update (기존 목록 삭제 후 새 목록 저장)
     *
     * @param userId  사용자 ID
     * @param request update 요청 DTO
     * @return 저장된 소셜 링크 목록
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 동일한 소셜 타입이 중복된 경우 (BAD_REQUEST)
     */
    List<UserResponse.SocialLinkItem> update(Long userId, List<UserRequest.UpdateSocialLink> request);

    /**
     * 특정 사용자의 소셜 링크 목록 조회 (게스트용)
     *
     * @param nickname 사용자 닉네임
     * @return 소셜 링크 목록
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     */
    List<UserResponse.SocialLinkItem> getPublicLinks(String nickname);
}
