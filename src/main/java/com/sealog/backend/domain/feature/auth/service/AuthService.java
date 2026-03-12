package com.sealog.backend.domain.feature.auth.service;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse;
import com.sealog.backend.domain.feature.auth.dto.TokenResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;

public interface AuthService {

    /**
     * 내 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 프로필
     * @throws CustomException 사용자를 찾을 수 없는 경우 (UNAUTHORIZED)
     */
    AuthResponse.AuthProfile getMe(Long userId);

    /**
     * 로그인
     * @param request 로그인 요청 DTO
     * @return 토큰 및 사용자 정보 (컨트롤러에서 쿠키 설정)
     * @throws CustomException 사용자를 찾을 수 없는 경우 (UNAUTHORIZED)
     * @throws CustomException 비밀번호가 일치하지 않는 경우 (UNAUTHORIZED)
     */
    TokenResponse login(AuthRequest.Login request);

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 엔티티
     * @throws CustomException 이메일이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     */
    User signUp(AuthRequest.SignUp request);

    /**
     * 토큰 재발급
     * - JWT 검증 및 DB 비교 포함
     * @param refreshToken 쿠키에서 추출한 Refresh Token
     * @return 새 Access Token 및 사용자 정보 (refreshToken은 null)
     * @throws CustomException 토큰이 유효하지 않거나 DB 토큰과 불일치 (UNAUTHORIZED)
     */
    TokenResponse refresh(String refreshToken);

    /**
     * 로그아웃
     * - 토큰이 유효한 경우 DB에서 Refresh Token 삭제
     * @param refreshToken 쿠키에서 추출한 Refresh Token
     */
    void logout(String refreshToken);
}
