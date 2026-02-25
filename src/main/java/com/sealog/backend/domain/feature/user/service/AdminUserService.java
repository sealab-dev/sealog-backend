package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;

public interface AdminUserService {
    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 엔티티 (컨트롤러에서 쿠키 설정)
     * @throws CustomException 이메일이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     */
    User signUp(AuthRequest.SignUpRequest request);
}
