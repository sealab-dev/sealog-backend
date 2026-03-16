package com.sealog.backend.domain.feature.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("User 통합 테스트 (Controller → Service → Repository)")
class UserIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
    }

    // =========================================================
    // 공개 프로필 조회
    // =========================================================

    @Nested
    @DisplayName("공개 프로필 조회 GET /api/users/{nickname}/profile")
    class 공개_프로필_조회 {

        @Test
        @DisplayName("성공 - 존재하는 닉네임 → 200, 공개 프로필 반환")
        void 성공() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/{nickname}/profile", testUser.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nickname").value(testUser.getNickname()))
                    .andExpect(jsonPath("$.data.position").exists())
                    .andExpect(jsonPath("$.data.socialLinks").isArray());
        }

        @Test
        @DisplayName("성공 - 응답에 민감 정보(email, password) 미포함")
        void 민감_정보_미포함() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/{nickname}/profile", testUser.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").doesNotExist())
                    .andExpect(jsonPath("$.data.password").doesNotExist());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 닉네임 → 404")
        void 존재하지_않는_닉네임() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/{nickname}/profile", "nobody"))
                    .andExpect(status().isNotFound());
        }
    }
}
