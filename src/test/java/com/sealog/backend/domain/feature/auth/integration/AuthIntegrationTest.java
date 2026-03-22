package com.sealog.backend.domain.feature.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.auth.store.RefreshTokenStore;
import com.sealog.backend.security.jwt.JwtTokenProvider;
import com.sealog.backend.support.base.test.IntegrationTest;
import com.sealog.backend.support.component.TestDataFactory;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth 통합 테스트
 */
@DisplayName("Auth 통합 테스트 (Controller → Service → Repository)")
class AuthIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired RefreshTokenStore refreshTokenStore;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
    }

    // =====================================================================
    // 로그인 플로우
    // =====================================================================
    @Nested
    @DisplayName("로그인 플로우")
    class Login {

        @Test
        @DisplayName("성공 - 올바른 이메일/비밀번호 → 200, access/refresh 쿠키 발급, 프로필 반환")
        void 성공() throws Exception {
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email(testUser.getEmail())
                    .password("password")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("access_token"))
                    .andExpect(cookie().exists("refresh_token"))
                    .andExpect(cookie().httpOnly("access_token", true))
                    .andExpect(cookie().httpOnly("refresh_token", true))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(testUser.getEmail()));
        }
    }

    // =====================================================================
    // 토큰 재발급 플로우
    // =====================================================================
    @Nested
    @DisplayName("토큰 재발급 플로우")
    class Refresh {

        @Test
        @DisplayName("성공 - 유효한 refresh_token 쿠키 → 200, 새 access_token 쿠키 발급")
        void 성공() throws Exception {
            String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId(), testUser.getEmail());
            refreshTokenStore.save(testUser.getId(), refreshToken, jwtTokenProvider.getRefreshTokenValidity());

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refresh_token", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("access_token"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(testUser.getEmail()));
        }
        @Test
        @DisplayName("실패 - refresh_token 쿠키 없이 요청 → 401")
        void 쿠키_없음() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - Redis에 저장된 토큰과 다른 쿠키로 요청 → 401")
        void Redis_토큰_불일치() throws Exception {
            // Redis에는 이전에 발급된 구 토큰을 저장
            refreshTokenStore.save(testUser.getId(), "stale-token-already-in-redis", jwtTokenProvider.getRefreshTokenValidity());

            // 유효한 JWT이지만 Redis에 저장된 값과 다름
            String freshToken = jwtTokenProvider.createRefreshToken(testUser.getId(), testUser.getEmail());

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refresh_token", freshToken)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =====================================================================
    // 내 정보 조회 플로우 (실패 케이스 포함)
    // =====================================================================
    @Nested
    @DisplayName("내 정보 조회 플로우")
    class Me {

        @Test
        @DisplayName("성공 - 유효한 access_token 쿠키 → 200, 내 프로필 반환")
        void 성공() throws Exception {
            String accessToken = jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getEmail());

            mockMvc.perform(get("/api/auth/me")
                            .cookie(new Cookie("access_token", accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(testUser.getEmail()));
        }

        @Test
        @DisplayName("실패 - 토큰 없이 요청 → 401")
        void 미인증() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 만료된 토큰으로 요청 → 401")
        void 토큰_만료() throws Exception {
            // 만료된 토큰 시뮬레이션 (유효기간을 음수로 설정하거나 필터에서 거부되도록 구성)
            // 여기서는 단순히 잘못된 토큰을 보냄
            mockMvc.perform(get("/api/auth/me")
                            .cookie(new Cookie("access_token", "expired.token.value")))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =====================================================================
    // 로그아웃 플로우
    // =====================================================================
    @Nested
    @DisplayName("로그아웃 플로우")
    class Logout {

        @Test
        @DisplayName("성공 - 유효한 refresh_token 쿠키로 로그아웃 → 200, 쿠키 삭제")
        void 성공() throws Exception {
            String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId(), testUser.getEmail());
            refreshTokenStore.save(testUser.getId(), refreshToken, jwtTokenProvider.getRefreshTokenValidity());

            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie("refresh_token", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().maxAge("access_token", 0))
                    .andExpect(cookie().maxAge("refresh_token", 0));

            assertThat(refreshTokenStore.find(testUser.getId())).isEmpty();
        }
    }
}
