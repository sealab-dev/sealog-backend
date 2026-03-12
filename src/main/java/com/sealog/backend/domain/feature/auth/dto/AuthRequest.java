package com.sealog.backend.domain.feature.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequest {

    /**
     * 회원가입 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원가입 요청")
    public static class SignUp {

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Schema(description = "사용자 이메일", example = "test2@local.com")
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요")
        @Schema(description = "비밀번호", example = "test1234")
        private String password;

        @NotBlank(message = "이름을 입력해주세요")
        @Size(min = 2, max = 20, message = "이름은 2~20자로 입력해주세요")
        @Schema(description = "이름", example = "홍길동")
        private String name;

        @NotBlank(message = "닉네임을 입력해주세요")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요")
        @Schema(description = "닉네임", example = "seadev")
        private String nickname;
    }

    /**
     * 로그인 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그인 요청")
    public static class Login {

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Schema(description = "사용자 이메일", example = "test@local.com")
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Schema(description = "비밀번호", example = "test1234")
        private String password;
    }
}