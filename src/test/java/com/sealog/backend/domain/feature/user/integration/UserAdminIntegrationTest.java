package com.sealog.backend.domain.feature.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.user.dto.UserAdminRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UserAdmin 통합 테스트 (Controller → Service → Repository)")
class UserAdminIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;
    @Autowired UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
    }

    // =========================================================
    // 사용자 생성 (Admin용)
    // =========================================================

    @Nested
    @DisplayName("사용자 생성")
    class 사용자_생성 {

        @Test
        @DisplayName("성공 - ADMIN 권한으로 사용자 생성 → 201, DB에 사용자 저장")
        void 성공() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email("newuser@local.com")
                    .password("password")
                    .name("새유저")
                    .nickname("newuser")
                    .build();

            mockMvc.perform(post("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));

            assertThat(userRepository.existsByEmail("newuser@local.com")).isTrue();
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 이메일로 생성 시도 → 409")
        void 이메일_중복() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email(testUser.getEmail())
                    .password("password")
                    .name("중복유저")
                    .nickname("duplicateUser")
                    .build();

            mockMvc.perform(post("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 일반 USER 권한으로 접근 → 403")
        void 권한_없음() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email("other@local.com")
                    .password("password")
                    .name("다른유저")
                    .nickname("otheruser")
                    .build();

            mockMvc.perform(post("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
