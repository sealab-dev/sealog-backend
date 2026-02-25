package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.user.dto.UserRequest;
import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;

public interface UserService {

    /**
     * 현재 로그인한 사용자 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     */
    UserResponse.UserInfo getMyInfo(Long userId);

    /**
     * 프로필 정보 수정 (닉네임 및/또는 프로필 이미지)
     * @param userId 사용자 ID
     * @param request 프로필 수정 요청 DTO (닉네임, fileId, profileImagePath)
     * @return 수정된 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 파일 업로드 실패 시 (INTERNAL_SERVER_ERROR)
     */
    UserResponse.UserInfo updateProfile(
            Long userId,
            UserRequest.UpdateProfileRequest request
    );

    /**
     * 비밀번호 변경
     * @param userId 사용자 ID
     * @param request 비밀번호 변경 요청 DTO
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 현재 비밀번호가 일치하지 않는 경우 (BAD_REQUEST)
     * @throws CustomException 새 비밀번호 확인이 일치하지 않는 경우 (BAD_REQUEST)
     */
    void changePassword(Long userId, UserRequest.ChangePasswordRequest request);

    /**
     * 현재 조회한 블로그 사용자 조회
     * @param nickname 사용자 닉네임
     * @return 닉네임, 프로필 이미지
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     */
    UserResponse.BlogUserInfo getBlogUser(String nickname);

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 엔티티 (컨트롤러에서 쿠키 설정)
     * @throws CustomException 이메일이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     */
    User signUp(AuthRequest.SignUpRequest request);
}
