package com.sealog.backend.domain.feature.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.user.dto.UserMeRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.SocialType;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.support.base.test.IntegrationTest;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("UserMe 통합 테스트 (Controller → Service → Repository)")
class UserMeIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
    }

    // =========================================================
    // 내 프로필 조회
    // =========================================================

    @Nested
    @DisplayName("내 프로필 조회 GET /api/me/profile")
    class 내_프로필_조회 {

        @Test
        @DisplayName("성공 - 인증된 사용자 요청 → 200, 내 정보 반환")
        void 성공() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);

            // when & then
            mockMvc.perform(get("/api/me/profile")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                    .andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
                    .andExpect(jsonPath("$.data.nickname").value(testUser.getNickname()));
        }
    }

    // =========================================================
    // 프로필 수정
    // =========================================================

    @Nested
    @DisplayName("프로필 수정 PATCH /api/me/profile")
    class 프로필_수정 {

        @Test
        @DisplayName("성공 - 닉네임 및 소셜 링크 수정 → 200")
        void 성공() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserMeRequest.UpdateProfile request = UserMeRequest.UpdateProfile.builder()
                    .nickname("새닉네임")
                    .socialLinks(List.of(
                            new UserMeRequest.UpdateSocialLink(SocialType.GITHUB, "https://github.com/testuser")
                    ))
                    .build();

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                    .andExpect(jsonPath("$.data.socialLinks.length()").value(1));
        }
    }

    // =========================================================
    // 비밀번호 변경
    // =========================================================

    @Nested
    @DisplayName("비밀번호 변경 PATCH /api/me/password")
    class 비밀번호_변경 {

        @Test
        @DisplayName("성공 - 올바른 비밀번호 정보로 변경 → 200")
        void 성공() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserMeRequest.UpdatePassword request = UserMeRequest.UpdatePassword.builder()
                    .currentPassword("password")
                    .newPassword("newPassword123")
                    .newPasswordConfirm("newPassword123")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/me/password")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    private MockMultipartFile toMultipartJson(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name, "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value)
        );
    }
}
