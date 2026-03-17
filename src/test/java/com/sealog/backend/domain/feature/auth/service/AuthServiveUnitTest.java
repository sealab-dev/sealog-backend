package com.sealog.backend.domain.feature.auth.service;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.dto.AuthResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.domain.feature.user.service.UserValidatorService;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.redis.repository.RefreshTokenStore;
import com.sealog.backend.infra.storage.service.FileStorageService;
import com.sealog.backend.security.jwt.JwtTokenProvider;
import com.sealog.backend.support.base.TestUnitBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("AuthService 단위 테스트")
class AuthServiveUnitTest extends TestUnitBase {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserValidatorService userValidatorService;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock FileStorageService fileStorageService;
    @Mock
    RefreshTokenStore refreshTokenStore;

    @InjectMocks
    AuthServiceImpl authService;

    private User testUser;

    private static final String TEST_EMAIL    = "test@local.com";
    private static final String TEST_PASSWORD = "test1234";
    private static final String ENCODED_PW    = "encodedPassword";
    private static final String ACCESS_TOKEN  = "access.token.value";
    private static final String REFRESH_TOKEN = "refresh.token.value";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email(TEST_EMAIL)
                .password(ENCODED_PW)
                .name("테스트유저")
                .nickname("tester")
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    // =====================================================================
    // 로그인
    // =====================================================================
    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("성공 - 올바른 이메일/비밀번호로 로그인하면 토큰과 프로필을 반환한다")
        void 성공() {
            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PW)).willReturn(true);
            given(jwtTokenProvider.createAccessToken(1L, TEST_EMAIL)).willReturn(ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(1L, TEST_EMAIL)).willReturn(REFRESH_TOKEN);
            given(jwtTokenProvider.getRefreshTokenValidity()).willReturn(86400000L);

            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email(TEST_EMAIL).password(TEST_PASSWORD).build();

            AuthResponse.Token result = authService.login(request);

            assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(result.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(result.getAuthProfile().getEmail()).isEqualTo(TEST_EMAIL);
            // Redis에 리프레시 토큰이 저장되었는지 확인
            verify(refreshTokenStore).save(eq(1L), eq(REFRESH_TOKEN), eq(86400000L));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 이메일로 로그인하면 401 예외가 발생한다")
        void 이메일_없음() {
            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email("nobody@local.com").password(TEST_PASSWORD).build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                        assertThat(ce.getMessage()).contains("이메일 또는 비밀번호가 일치하지 않습니다");
                    });
        }

        @Test
        @DisplayName("실패 - 비밀번호가 틀리면 401 예외가 발생한다")
        void 비밀번호_불일치() {
            given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongPw", ENCODED_PW)).willReturn(false);

            AuthRequest.Login request = AuthRequest.Login.builder()
                    .email(TEST_EMAIL).password("wrongPw").build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                        assertThat(ce.getMessage()).contains("이메일 또는 비밀번호가 일치하지 않습니다");
                    });
        }
    }

    // =====================================================================
    // 토큰 재발급
    // =====================================================================
    @Nested
    @DisplayName("토큰 재발급")
    class Refresh {

        @BeforeEach
        void givenStoredToken() {
            given(refreshTokenStore.find(1L)).willReturn(Optional.of(REFRESH_TOKEN));
        }

        @Test
        @DisplayName("성공 - 유효한 리프레시 토큰으로 새 액세스 토큰이 발급된다")
        void 성공() {
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserId(REFRESH_TOKEN)).willReturn(1L);
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(jwtTokenProvider.createAccessToken(1L, TEST_EMAIL)).willReturn("new.access.token");

            AuthResponse.Token result = authService.refresh(REFRESH_TOKEN);

            assertThat(result.getAccessToken()).isEqualTo("new.access.token");
            assertThat(result.getRefreshToken()).isNull();   // refresh 시 리프레시 토큰은 재발급 안 함
            assertThat(result.getAuthProfile().getEmail()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("실패 - 서명이 유효하지 않은 토큰으로 재발급하면 401 예외가 발생한다")
        void 유효하지_않은_토큰() {
            given(jwtTokenProvider.validateToken("bad.token")).willReturn(false);

            assertThatThrownBy(() -> authService.refresh("bad.token"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED));
        }

        @Test
        @DisplayName("실패 - Redis에 저장된 토큰과 다른 토큰으로 재발급하면 401 예외가 발생한다")
        void Redis_토큰_불일치() {
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserId(REFRESH_TOKEN)).willReturn(1L);
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(refreshTokenStore.find(1L)).willReturn(Optional.of("token.saved.in.redis"));

            assertThatThrownBy(() -> authService.refresh(REFRESH_TOKEN))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED));
        }
    }

    // =====================================================================
    // 로그아웃
    // =====================================================================
    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("성공 - 유효한 토큰으로 로그아웃하면 Redis의 리프레시 토큰이 삭제된다")
        void 성공() {
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserId(REFRESH_TOKEN)).willReturn(1L);

            authService.logout(REFRESH_TOKEN);

            verify(refreshTokenStore).delete(1L);
        }

        @Test
        @DisplayName("예외없음 - 유효하지 않은 토큰으로 로그아웃해도 예외가 발생하지 않는다")
        void 유효하지_않은_토큰_무시() {
            given(jwtTokenProvider.validateToken("invalid.token")).willReturn(false);

            assertThatCode(() -> authService.logout("invalid.token"))
                    .doesNotThrowAnyException();

            // userId 추출 → Redis 삭제까지 진행되면 안 됨
            verify(refreshTokenStore, never()).delete(anyLong());
        }
    }
}
