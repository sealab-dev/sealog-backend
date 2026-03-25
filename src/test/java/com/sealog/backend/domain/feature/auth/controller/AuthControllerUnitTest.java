package com.sealog.backend.domain.feature.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse;
import com.sealog.backend.domain.feature.auth.service.AuthService;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.global.exception.GlobalExceptionHandler;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.security.util.CookieUtil;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Auth 컨트롤러 단위 테스트 - standalone")
class AuthControllerUnitTest extends UnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private CookieUtil cookieUtil;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler(), new com.sealog.backend.global.response.ResponseWrapperAdvice())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("로그인 POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("성공 - 올바른 정보로 로그인 → 200")
        void 로그인_성공() throws Exception {
            // given
            AuthRequest.Login request = new AuthRequest.Login("test@local.com", "password123");
            AuthResponse.AuthProfile profile = AuthResponse.AuthProfile.of(1L, "test@local.com", "Name", "Nick", UserRole.USER, null);
            AuthResponse.Token token = AuthResponse.Token.of("access", "refresh", profile);

            given(authService.login(any())).willReturn(token);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("test@local.com"));
        }

        @Test
        @DisplayName("실패 - 이메일 공백 → 400")
        void 이메일_공백() throws Exception {
            AuthRequest.Login request = new AuthRequest.Login("", "password123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이메일을 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 비밀번호 공백 → 400")
        void 비밀번호_공백() throws Exception {
            AuthRequest.Login request = new AuthRequest.Login("test@local.com", "");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("비밀번호를 입력해주세요"));
        }
    }

    @Nested
    @DisplayName("토큰 재발급 POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("실패 - 쿠키에 토큰 없음 → 401")
        void 쿠키_토큰_없음() throws Exception {
            given(cookieUtil.getRefreshToken(any())).willReturn(Optional.empty());

            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Refresh Token이 없습니다"));
        }
    }

    @Nested
    @DisplayName("로그아웃 POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("성공 - 쿠키에 토큰이 있으면 로그아웃 처리 후 200")
        void 로그아웃_성공() throws Exception {
            given(cookieUtil.getRefreshToken(any())).willReturn(Optional.of("valid.refresh.token"));

            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk());

            verify(authService).logout("valid.refresh.token");
        }

        @Test
        @DisplayName("성공 - 쿠키에 토큰이 없어도 200을 반환한다")
        void 토큰_없어도_성공() throws Exception {
            given(cookieUtil.getRefreshToken(any())).willReturn(Optional.empty());

            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk());

            verify(authService, never()).logout(anyString());
        }
    }

    @Nested
    @DisplayName("내 정보 조회 GET /api/auth/me")
    class GetMe {

        @Test
        @DisplayName("성공 - 인증된 사용자 정보 조회 → 200")
        void 내정보_조회_성공() throws Exception {
            // given
            Long userId = 1L;
            User user = User.builder().email("test@local.com").role(UserRole.USER).build();
            org.springframework.test.util.ReflectionTestUtils.setField(user, "id", userId);
            CustomUserDetails userDetails = new CustomUserDetails(user);

            // Standalone에서 AuthenticationPrincipal을 위해 SecurityContext 설정
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
            );

            AuthResponse.AuthProfile profile = AuthResponse.AuthProfile.of(userId, "test@local.com", "Name", "Nick", UserRole.USER, null);
            given(authService.getMe(userId)).willReturn(profile);

            // when & then
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(userId));
        }
    }
}
