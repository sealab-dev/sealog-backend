package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.user.dto.AuthRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;

public interface AuthService {

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 엔티티 (컨트롤러에서 쿠키 설정)
     * @throws CustomException 이메일이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     */
    User signUp(AuthRequest.SignUpRequest request);

    /**
     * 로그인
     * @param request 로그인 요청 DTO
     * @return 인증된 사용자 엔티티 (컨트롤러에서 쿠키 설정)
     * @throws CustomException 사용자를 찾을 수 없는 경우 (UNAUTHORIZED)
     * @throws CustomException 비밀번호가 일치하지 않는 경우 (UNAUTHORIZED)
     */
    User login(AuthRequest.LoginRequest request);

    /**
     * 토큰 재발급을 위한 사용자 조회
     * @param userId Refresh Token에서 추출한 사용자 ID
     * @return 사용자 엔티티
     * @throws CustomException 사용자를 찾을 수 없는 경우 (UNAUTHORIZED)
     */
    User getUserForRefresh(Long userId);

    /**
     * Refresh Token을 DB에 저장 (로그인 시 호출)
     * @param userId 사용자 ID
     * @param refreshToken 저장할 Refresh Token
     */
    void saveRefreshToken(Long userId, String refreshToken);

    /**
     * DB에 저장된 Refresh Token과 비교 검증 (재발급 시 호출)
     * @param userId 사용자 ID
     * @param refreshToken 쿠키에서 추출한 Refresh Token
     * @throws CustomException 토큰이 일치하지 않거나 DB 토큰이 없는 경우 (UNAUTHORIZED)
     */
    void validateStoredRefreshToken(Long userId, String refreshToken);

    /**
     * DB에서 Refresh Token 삭제 (로그아웃 시 호출)
     * @param userId 사용자 ID
     */
    void deleteRefreshToken(Long userId);

}