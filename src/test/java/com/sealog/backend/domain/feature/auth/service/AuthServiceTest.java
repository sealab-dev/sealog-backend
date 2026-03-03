package com.sealog.backend.domain.feature.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.security.jwt.JwtTokenProvider;
import com.sealog.backend.support.AbstractContainerTest;
import com.sealog.backend.support.ExecutionTimeExtension;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth 통합 테스트
 *
 * Controller → Service → Repository 전체 레이어를 관통하는 통합 테스트.
 * AbstractContainerTest를 통해 MariaDB / Redis Testcontainer를 사용한다.
 * 각 테스트는 @Transactional로 격리되며 종료 시 자동 롤백된다.
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  주의: AWS S3/CloudFront 빈 생성에 실패하는 경우               │
 * │  해당 StorageService를 @MockBean으로 추가해주세요.              │
 * └──────────────────────────────────────────────────────────────────┘
 */
@Tag("integration")
@ExtendWith(ExecutionTimeExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth 통합 테스트 (Controller → Service → Repository)")
class AuthServiceTest extends AbstractContainerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtTokenProvider jwtTokenProvider;

    private User testUser;

    private static final String TEST_EMAIL    = "flow-test@local.com";
    private static final String TEST_PASSWORD = "test1234";

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(
                User.builder()
                        .email(TEST_EMAIL)
                        .password(passwordEncoder.encode(TEST_PASSWORD))
                        .name("통합테스터")
                        .nickname("flowtester")
                        .role(UserRole.USER)
                        .build()
        );
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
            AuthRequest.LoginRequest request = AuthRequest.LoginRequest.builder()
                    .email(TEST_EMAIL).password(TEST_PASSWORD).build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("access_token"))
                    .andExpect(cookie().exists("refresh_token"))
                    .andExpect(cookie().httpOnly("access_token", true))
                    .andExpect(cookie().httpOnly("refresh_token", true))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.message").value("로그인 성공"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 이메일 → 401")
        void 이메일_없음() throws Exception {
            AuthRequest.LoginRequest request = AuthRequest.LoginRequest.builder()
                    .email("nobody@local.com").password(TEST_PASSWORD).build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 잘못된 비밀번호 → 401")
        void 비밀번호_불일치() throws Exception {
            AuthRequest.LoginRequest request = AuthRequest.LoginRequest.builder()
                    .email(TEST_EMAIL).password("wrongPassword1").build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 이메일 형식 → 400")
        void 이메일_형식_오류() throws Exception {
            AuthRequest.LoginRequest request = AuthRequest.LoginRequest.builder()
                    .email("not-an-email").password(TEST_PASSWORD).build();

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
            // 실제 JWT 리프레시 토큰 생성 후 DB에 저장
            String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId(), TEST_EMAIL);
            testUser.updateRefreshToken(refreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refresh_token", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("access_token"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(TEST_EMAIL));
        }

        @Test
        @DisplayName("실패 - refresh_token 쿠키 없이 요청 → 401")
        void 쿠키_없음() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - DB에 저장된 토큰과 다른 쿠키로 요청 → 401")
        void DB_토큰_불일치() throws Exception {
            // DB에는 이전에 발급된 토큰(구 토큰)이 저장되어 있는 상태
            // createRefreshToken을 같은 밀리초 내에 두 번 호출하면 iat가 동일해 토큰이 일치할 수 있으므로
            // DB에는 JWT가 아닌 고정 문자열을 저장해 확실하게 불일치 시나리오를 만든다
            testUser.updateRefreshToken("stale-token-already-in-db");

            // 유효한 JWT이지만 DB에 저장된 값과 다름
            String freshToken = jwtTokenProvider.createRefreshToken(testUser.getId(), TEST_EMAIL);

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
        @DisplayName("성공 - 유효한 refresh_token 쿠키로 로그아웃 → 200, 쿠키 삭제, DB 토큰 제거")
        void 성공() throws Exception {
            String refreshToken = jwtTokenProvider.createRefreshToken(testUser.getId(), TEST_EMAIL);
            testUser.updateRefreshToken(refreshToken);

            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie("refresh_token", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().maxAge("access_token", 0))
                    .andExpect(cookie().maxAge("refresh_token", 0))
                    .andExpect(jsonPath("$.success").value(true));

            // DB에서 리프레시 토큰이 제거되었는지 확인
            User updated = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
            assertThat(updated.getRefreshToken()).isNull();
        }

        @Test
        @DisplayName("성공 - 쿠키 없이 로그아웃 요청해도 200 반환 (쿠키만 삭제)")
        void 쿠키_없이_로그아웃() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // =====================================================================
    // 회원가입 플로우 (Admin)
    // =====================================================================
    @Nested
    @DisplayName("회원가입 플로우")
    class SignUp {

        @Test
        @DisplayName("성공 - ADMIN 권한으로 회원가입 → 200, DB에 사용자 저장")
        void 성공() throws Exception {
            AuthRequest.SignUpRequest request = AuthRequest.SignUpRequest.builder()
                    .email("newuser@local.com")
                    .password("newpassword1")
                    .name("새유저")
                    .nickname("newuser")
                    .build();

            mockMvc.perform(post("/api/admin/auth/signup")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            assertThat(userRepository.existsByEmail("newuser@local.com")).isTrue();
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 이메일로 가입 시도 → 409")
        void 이메일_중복() throws Exception {
            // setUp()에서 TEST_EMAIL 사용자가 이미 저장되어 있음
            AuthRequest.SignUpRequest request = AuthRequest.SignUpRequest.builder()
                    .email(TEST_EMAIL)
                    .password("password1234")
                    .name("중복유저")
                    .nickname("duplicateUser")
                    .build();

            mockMvc.perform(post("/api/admin/auth/signup")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패 - USER 권한으로 회원가입 요청 → 403")
        void 권한_없음() throws Exception {
            AuthRequest.SignUpRequest request = AuthRequest.SignUpRequest.builder()
                    .email("someone@local.com")
                    .password("password1234")
                    .name("유저")
                    .nickname("someone")
                    .build();

            mockMvc.perform(post("/api/admin/auth/signup")
                            .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 유효성 검사 위반 (짧은 비밀번호, 잘못된 이메일) → 400")
        void 유효성_검사_실패() throws Exception {
            AuthRequest.SignUpRequest request = AuthRequest.SignUpRequest.builder()
                    .email("not-an-email")  // 이메일 형식 아님
                    .password("short")      // 8자 미만
                    .name("a")              // 2자 미만
                    .nickname("b")          // 2자 미만
                    .build();

            mockMvc.perform(post("/api/admin/auth/signup")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
