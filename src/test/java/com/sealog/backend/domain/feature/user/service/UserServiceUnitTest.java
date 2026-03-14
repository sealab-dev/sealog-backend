package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.user.dto.UserRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("UserService 단위 테스트")
class UserServiceUnitTest extends TestUnitBase {

    @Mock UserRepository userRepository;
    @Mock UserValidatorService userValidatorService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserFileService userFileService;
    @Mock UserSocialService userSocialService;

    @InjectMocks
    UserServiceImpl userService;

    private User testUser;

    private static final String TEST_EMAIL    = "test@local.com";
    private static final String TEST_PASSWORD = "test1234";
    private static final String ENCODED_PW    = "encodedPassword";

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
    // 사용자 생성 (Admin용)
    // =====================================================================
    @Nested
    @DisplayName("사용자 생성")
    class CreateUser {

        @Test
        @DisplayName("성공 - 올바른 정보로 사용자를 생성한다")
        void 성공() {
            given(passwordEncoder.encode(TEST_PASSWORD)).willReturn(ENCODED_PW);
            given(userRepository.save(any(User.class))).willReturn(testUser);

            UserRequest.Create request = UserRequest.Create.builder()
                    .email(TEST_EMAIL)
                    .password(TEST_PASSWORD)
                    .name("테스트유저")
                    .nickname("tester")
                    .build();

            userService.createUser(request);

            verify(userValidatorService).validateDuplicateEmail(TEST_EMAIL);
            verify(userValidatorService).validateDuplicateNickname("tester");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("실패 - 중복 이메일로 생성하면 409 예외가 발생한다")
        void 이메일_중복() {
            doThrow(CustomException.conflict("이미 사용 중인 이메일입니다"))
                    .when(userValidatorService).validateDuplicateEmail(TEST_EMAIL);

            UserRequest.Create request = UserRequest.Create.builder()
                    .email(TEST_EMAIL).password(TEST_PASSWORD)
                    .name("테스트유저").nickname("tester").build();

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 중복 닉네임으로 생성하면 409 예외가 발생한다")
        void 닉네임_중복() {
            doThrow(CustomException.conflict("이미 사용 중인 닉네임입니다"))
                    .when(userValidatorService).validateDuplicateNickname("tester");

            UserRequest.Create request = UserRequest.Create.builder()
                    .email(TEST_EMAIL).password(TEST_PASSWORD)
                    .name("테스트유저").nickname("tester").build();

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));

            verify(userRepository, never()).save(any());
        }
    }
}
