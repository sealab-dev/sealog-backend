package com.sealog.backend.domain.feature.auth.service;

import com.sealog.backend.domain.feature.auth.dto.AuthRequest;
import com.sealog.backend.domain.feature.auth.dto.TokenResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.domain.feature.user.service.UserValidatorService;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.security.jwt.JwtTokenProvider;
import com.sealog.backend.support.ExecutionTimeExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith({MockitoExtension.class, ExecutionTimeExtension.class})
@DisplayName("AuthService 단위 테스트")
class AuthServiveUnitTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserValidatorService userValidatorService;
    @Mock JwtTokenProvider jwtTokenProvider;

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

            AuthRequest.LoginRequest request = AuthRequest.LoginRequest.builder()
                    .email(TEST_EMAIL).password(TEST_PASSWORD).build();

            TokenResponse result = authService.login(request);

            assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(result.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(result.getProfile().getEmail()).isEqualTo(TEST_EMAIL);
            // DB에 리프레시 토큰이 저장되었는지 확인
            assertThat(testUser.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 이메일로 로그인하면 401 예외가 발생한다")
        void 이메일_없음() {
            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

            AuthRequest.LoginRequest request = AuthRequest.LoginRequest.builder()
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

            AuthRequest.LoginRequest request = AuthRequest.LoginRequest.builder()
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
            testUser.updateRefreshToken(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("성공 - 유효한 리프레시 토큰으로 새 액세스 토큰이 발급된다")
        void 성공() {
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserId(REFRESH_TOKEN)).willReturn(1L);
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(jwtTokenProvider.createAccessToken(1L, TEST_EMAIL)).willReturn("new.access.token");

            TokenResponse result = authService.refresh(REFRESH_TOKEN);

            assertThat(result.getAccessToken()).isEqualTo("new.access.token");
            assertThat(result.getRefreshToken()).isNull();   // refresh 시 리프레시 토큰은 재발급 안 함
            assertThat(result.getProfile().getEmail()).isEqualTo(TEST_EMAIL);
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
        @DisplayName("실패 - DB에 저장된 토큰과 다른 토큰으로 재발급하면 401 예외가 발생한다")
        void DB_토큰_불일치() {
            testUser.updateRefreshToken("token.saved.in.db");

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserId(REFRESH_TOKEN)).willReturn(1L);
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

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

        @BeforeEach
        void givenStoredToken() {
            testUser.updateRefreshToken(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("성공 - 유효한 토큰으로 로그아웃하면 DB의 리프레시 토큰이 삭제된다")
        void 성공() {
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserId(REFRESH_TOKEN)).willReturn(1L);
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            authService.logout(REFRESH_TOKEN);

            assertThat(testUser.getRefreshToken()).isNull();
        }

        @Test
        @DisplayName("예외없음 - 유효하지 않은 토큰으로 로그아웃해도 예외가 발생하지 않는다")
        void 유효하지_않은_토큰_무시() {
            given(jwtTokenProvider.validateToken("invalid.token")).willReturn(false);

            assertThatCode(() -> authService.logout("invalid.token"))
                    .doesNotThrowAnyException();

            // userId 추출 → DB 조회까지 진행되면 안 됨
            verify(userRepository, never()).findById(anyLong());
        }
    }

    // =====================================================================
    // 회원가입
    // =====================================================================
    @Nested
    @DisplayName("회원가입")
    class SignUp {

        @Test
        @DisplayName("성공 - 올바른 정보로 회원가입하면 저장된 User를 반환한다")
        void 성공() {
            given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(ENCODED_PW);
            given(userRepository.save(any(User.class))).willReturn(testUser);

            AuthRequest.SignUpRequest request = AuthRequest.SignUpRequest.builder()
                    .email(TEST_EMAIL)
                    .password(TEST_PASSWORD)
                    .name("테스트유저")
                    .nickname("tester")
                    .build();

            User result = authService.signUp(request);

            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            verify(userValidatorService).validateDuplicateEmail(TEST_EMAIL);
            verify(userValidatorService).validateDuplicateNickname("tester");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("실패 - 중복 이메일로 회원가입하면 409 예외가 발생한다")
        void 이메일_중복() {
            doThrow(CustomException.conflict("이미 사용 중인 이메일입니다"))
                    .when(userValidatorService).validateDuplicateEmail(TEST_EMAIL);

            AuthRequest.SignUpRequest request = AuthRequest.SignUpRequest.builder()
                    .email(TEST_EMAIL).password(TEST_PASSWORD)
                    .name("테스트유저").nickname("tester").build();

            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));

            // 중복 이메일이므로 save까지 가면 안 됨
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 중복 닉네임으로 회원가입하면 409 예외가 발생한다")
        void 닉네임_중복() {
            doThrow(CustomException.conflict("이미 사용 중인 닉네임입니다"))
                    .when(userValidatorService).validateDuplicateNickname("tester");

            AuthRequest.SignUpRequest request = AuthRequest.SignUpRequest.builder()
                    .email(TEST_EMAIL).password(TEST_PASSWORD)
                    .name("테스트유저").nickname("tester").build();

            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));

            verify(userRepository, never()).save(any());
        }
    }
}
