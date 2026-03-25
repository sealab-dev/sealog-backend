package com.sealog.backend.domain.feature.user.unit.service;

import com.sealog.backend.domain.feature.user.dto.UserAdminRequest;
import com.sealog.backend.domain.feature.user.dto.UserMeRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.domain.feature.user.service.UserFileService;
import com.sealog.backend.domain.feature.user.service.UserServiceImpl;
import com.sealog.backend.domain.feature.user.service.UserSocialService;
import com.sealog.backend.domain.feature.user.service.UserValidatorService;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.service.FileStorageService;
import com.sealog.backend.support.base.test.UnitTest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("UserService 단위 테스트")
class UserServiceUnitTest extends UnitTest {

    @Mock UserRepository userRepository;
    @Mock
    UserValidatorService userValidatorService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock
    UserFileService userFileService;
    @Mock
    UserSocialService userSocialService;
    @Mock FileStorageService fileStorageService;

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
    // 내 프로필 조회
    // =====================================================================
    @Nested
    @DisplayName("내 프로필 조회")
    class GetMyProfile {

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자 ID → 404 예외")
        void 사용자_없음() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getMyProfile(1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));
        }
    }

    // =====================================================================
    // 공개 프로필 조회
    // =====================================================================
    @Nested
    @DisplayName("공개 프로필 조회")
    class GetPublicProfile {

        @Test
        @DisplayName("실패 - 존재하지 않는 닉네임 → 404 예외")
        void 닉네임_없음() {
            given(userRepository.findByNickname("unknown")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getPublicProfile("unknown"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));
        }
    }

    // =====================================================================
    // 프로필 수정
    // =====================================================================
    @Nested
    @DisplayName("프로필 수정")
    class UpdateProfile {

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자 ID → 404 예외")
        void 사용자_없음() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            UserMeRequest.UpdateProfile request = UserMeRequest.UpdateProfile.builder()
                    .nickname("새닉네임").build();

            assertThatThrownBy(() -> userService.updateProfile(1L, request, null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 이미 사용 중인 닉네임으로 변경 시 409 예외")
        void 닉네임_중복() {
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            doThrow(CustomException.conflict("이미 사용 중인 닉네임입니다"))
                    .when(userValidatorService).validateDuplicateNickname("중복닉네임");

            UserMeRequest.UpdateProfile request = UserMeRequest.UpdateProfile.builder()
                    .nickname("중복닉네임").build();

            assertThatThrownBy(() -> userService.updateProfile(1L, request, null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));

            verify(userRepository, never()).save(any());
        }
    }

    // =====================================================================
    // 비밀번호 변경
    // =====================================================================
    @Nested
    @DisplayName("비밀번호 변경")
    class UpdatePassword {

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자 ID → 404 예외")
        void 사용자_없음() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            UserMeRequest.UpdatePassword request = UserMeRequest.UpdatePassword.builder()
                    .currentPassword("password").newPassword("newpass123").newPasswordConfirm("newpass123").build();

            assertThatThrownBy(() -> userService.updatePassword(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 현재 비밀번호 불일치 → 400 예외")
        void 현재_비밀번호_불일치() {
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("wrongPassword", ENCODED_PW)).willReturn(false);

            UserMeRequest.UpdatePassword request = UserMeRequest.UpdatePassword.builder()
                    .currentPassword("wrongPassword").newPassword("newpass123").newPasswordConfirm("newpass123").build();

            assertThatThrownBy(() -> userService.updatePassword(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ce.getMessage()).contains("현재 비밀번호가 일치하지 않습니다");
                    });

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 새 비밀번호 확인 불일치 → 400 예외")
        void 새_비밀번호_확인_불일치() {
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password", ENCODED_PW)).willReturn(true);

            UserMeRequest.UpdatePassword request = UserMeRequest.UpdatePassword.builder()
                    .currentPassword("password").newPassword("newpass123").newPasswordConfirm("different456").build();

            assertThatThrownBy(() -> userService.updatePassword(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ce.getMessage()).contains("새 비밀번호가 일치하지 않습니다");
                    });

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 새 비밀번호가 현재 비밀번호와 동일 → 400 예외")
        void 새_비밀번호_현재와_동일() {
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches("password", ENCODED_PW)).willReturn(true);

            UserMeRequest.UpdatePassword request = UserMeRequest.UpdatePassword.builder()
                    .currentPassword("password").newPassword("password").newPasswordConfirm("password").build();

            assertThatThrownBy(() -> userService.updatePassword(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ce.getMessage()).contains("새 비밀번호는 현재 비밀번호와 달라야 합니다");
                    });

            verify(userRepository, never()).save(any());
        }
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

            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
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

            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
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

            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
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
