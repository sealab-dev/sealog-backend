package com.sealog.backend.domain.feature.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.domain.feature.auth.store.RefreshTokenStore;
import com.sealog.backend.security.jwt.JwtTokenProvider;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.component.TestDataFactory;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth 통합 테스트
 */
@DisplayName("Auth 통합 테스트 (Controller → Service → Repository)")
class AuthIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired
    RefreshTokenStore refreshTokenStore;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
    }

    @Test
    @Order(0)
    void warmUp() {
        // 아무것도 안 함, JVM 웜업용
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
                    .password("password")  // TestDataFactory 기본 패스워드
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

        @Test
        @DisplayName("실패 - 존재하지 않는 이메일 → 400")
        void 이메일_없음() throws Exception {
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email("nobody@local.com")
                    .password("password")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 잘못된 비밀번호 → 400")
        void 비밀번호_불일치() throws Exception {
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email(testUser.getEmail())
                    .password("pass")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 이메일 형식 → 400")
        void 이메일_형식_오류() throws Exception {
            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email("not-an-email")
                    .password("password")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
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
            // 실제 JWT 리프레시 토큰 생성 후 Redis에 저장
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
    // 로그아웃 플로우
    // =====================================================================
    @Nested
    @DisplayName("로그아웃 플로우")
    class Logout {

        @Test
        @DisplayName("성공 - 유효한 refresh_token 쿠키로 로그아웃 → 200, 쿠키 삭제, Redis 토큰 제거")
        void 성공() throws Exception {
            String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId(), testUser.getEmail());
            refreshTokenStore.save(testUser.getId(), refreshToken, jwtTokenProvider.getRefreshTokenValidity());

            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie("refresh_token", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().maxAge("access_token", 0))
                    .andExpect(cookie().maxAge("refresh_token", 0))
                    .andExpect(jsonPath("$.success").value(true));

            // Redis에서 리프레시 토큰이 제거되었는지 확인
            assertThat(refreshTokenStore.find(testUser.getId())).isEmpty();
        }

        @Test
        @DisplayName("성공 - 쿠키 없이 로그아웃 요청해도 200 반환 (쿠키만 삭제)")
        void 쿠키_없이_로그아웃() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
