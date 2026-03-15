package com.sealog.backend.domain.feature.user.dto;

import com.sealog.backend.domain.feature.user.enums.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserMeRequest {

    /**
     * 프로필 수정 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로필 수정 요청")
    public static class UpdateProfile {

        @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요")
        @Schema(description = "닉네임", example = "seadev")
        private String nickname;

        @Size(max = 500, message = "소개는 500자 이내로 입력해주세요.")
        @Schema(description = "소개", example = "Java/Spring 기반 백엔드 개발자입니다.")
        private String about;

        @Schema(description = "프로필 이미지 제거 여부", example = "false")
        private Boolean removeProfileImage;

        @Valid
        @Schema(description = "소셜 링크 목록 (null이면 변경 없음, 빈 배열이면 전체 삭제)")
        private List<UpdateSocialLink> socialLinks;
    }

    /**
     * 비밀번호 변경 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "비밀번호 변경 요청")
    public static class UpdatePassword {

        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        @Schema(description = "현재 비밀번호", example = "test1234")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요")
        @Schema(description = "새 비밀번호", example = "newpass1234")
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요")
        @Schema(description = "새 비밀번호 확인", example = "newpass1234")
        private String newPasswordConfirm;
    }

    /**
     * 소셜 링크
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소셜 링크 단건")
    public static class UpdateSocialLink {

        @NotNull(message = "소셜 타입을 입력해주세요")
        @Schema(description = "소셜 타입", example = "GITHUB")
        private SocialType socialType;

        @NotNull(message = "URL을 입력해주세요")
        @Size(max = 500, message = "URL은 500자 이내로 입력해주세요")
        @Schema(description = "소셜 링크 URL", example = "https://github.com/username")
        private String url;
    }
}
