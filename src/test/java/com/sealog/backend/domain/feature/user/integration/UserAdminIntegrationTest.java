package com.sealog.backend.domain.feature.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.user.dto.UserAdminRequest;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.support.base.test.IntegrationTest;
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

@DisplayName("UserAdmin 통합 테스트")
class UserAdminIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @Nested
    @DisplayName("사용자 관리 (Admin)")
    class UserManagementAdmin {

        @Test
        @DisplayName("성공 - ADMIN 권한으로 새로운 사용자를 생성한다")
        void createUser_Success() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email("newadminuser@local.com")
                    .password("password")
                    .name("관리자생성유저")
                    .nickname("admincreated")
                    .build();

            mockMvc.perform(post("/api/admin/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));

            assertThat(userRepository.existsByEmail("newadminuser@local.com")).isTrue();
        }
    }
}
