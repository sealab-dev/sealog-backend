package com.sealog.backend.domain.feature.user.integration;

import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.support.base.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("User 통합 테스트")
class UserIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
    }

    @Nested
    @DisplayName("공개 프로필 조회")
    class PublicProfileInquiry {

        @Test
        @DisplayName("성공 - 존재하는 사용자의 공개 프로필을 조회한다")
        void getPublicProfile_Success() throws Exception {
            mockMvc.perform(get("/api/users/{nickname}/profile", testUser.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nickname").value(testUser.getNickname()))
                    .andExpect(jsonPath("$.data.email").doesNotExist());
        }
    }
}
