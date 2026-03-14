package com.sealog.backend.domain.feature.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.user.dto.UserRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserAdmin 통합 테스트
 */
@DisplayName("UserAdmin 통합 테스트 (Controller → Service → Repository)")
class UserAdminIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
    }

    @Test
    @Order(0)
    void warmUp() {
        // JVM 웜업
    }

    // =====================================================================
    // 사용자 생성 플로우 (Admin)
    // =====================================================================
    @Nested
    @DisplayName("사용자 생성")
    class CreateUser {

        @Test
        @DisplayName("성공 - ADMIN 권한으로 사용자 생성 → 200, DB에 사용자 저장")
        void 성공() throws Exception {
            UserRequest.Create request = UserRequest.Create.builder()
                    .email("newuser@local.com")
                    .password("password")
                    .name("새유저")
                    .nickname("newuser")
                    .build();

            mockMvc.perform(post("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            assertThat(userRepository.existsByEmail("newuser@local.com")).isTrue();
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 이메일로 생성 시도 → 409")
        void 이메일_중복() throws Exception {
            UserRequest.Create request = UserRequest.Create.builder()
                    .email(testUser.getEmail())
                    .password("password")
                    .name("중복유저")
                    .nickname("duplicateUser")
                    .build();

            mockMvc.perform(post("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패 - USER 권한으로 생성 요청 → 403")
        void 권한_없음() throws Exception {
            UserRequest.Create request = UserRequest.Create.builder()
                    .email("someone@local.com")
                    .password("password")
                    .name("유저")
                    .nickname("someone")
                    .build();

            mockMvc.perform(post("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 유효성 검사 위반 → 400")
        void 유효성_검사_실패() throws Exception {
            UserRequest.Create request = UserRequest.Create.builder()
                    .email("not-an-email")
                    .password("short")
                    .name("a")
                    .nickname("b")
                    .build();

            mockMvc.perform(post("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
