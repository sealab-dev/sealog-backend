package com.sealog.backend.domain.feature.user.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.user.controller.UserMeController;
import com.sealog.backend.domain.feature.user.dto.UserMeRequest;
import com.sealog.backend.domain.feature.user.service.UserService;
import com.sealog.backend.global.exception.GlobalExceptionHandler;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UserMeController 단위 테스트 - 유효성 검증")
class UserMeControllerUnitTest extends UnitTest {

    @Mock UserService userService;
    @InjectMocks
    UserMeController userMeController;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userMeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    // =====================================================================
    // 비밀번호 변경
    // =====================================================================
    @Nested
    @DisplayName("비밀번호 변경 PATCH /api/me/password - 유효성 검증 실패")
    class ChangePassword {

        @Test
        @DisplayName("실패 - 현재 비밀번호 공백 → 400")
        void 현재_비밀번호_공백() throws Exception {
            UserMeRequest.UpdatePassword request = UserMeRequest.UpdatePassword.builder()
                    .currentPassword("").newPassword("newpass123").newPasswordConfirm("newpass123").build();

            mockMvc.perform(patch("/api/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("현재 비밀번호를 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 새 비밀번호 8자 미만 → 400")
        void 새_비밀번호_너무_짧음() throws Exception {
            UserMeRequest.UpdatePassword request = UserMeRequest.UpdatePassword.builder()
                    .currentPassword("password").newPassword("1234567").newPasswordConfirm("1234567").build();

            mockMvc.perform(patch("/api/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("비밀번호는 8~20자로 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 새 비밀번호 20자 초과 → 400")
        void 새_비밀번호_너무_김() throws Exception {
            UserMeRequest.UpdatePassword request = UserMeRequest.UpdatePassword.builder()
                    .currentPassword("password").newPassword("a".repeat(21)).newPasswordConfirm("a".repeat(21)).build();

            mockMvc.perform(patch("/api/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("비밀번호는 8~20자로 입력해주세요"));
        }
    }

    // =====================================================================
    // 프로필 수정
    // =====================================================================
    @Nested
    @DisplayName("프로필 수정 PATCH /api/me/profile - 유효성 검증 실패")
    class UpdateProfile {

        @Test
        @DisplayName("실패 - 닉네임 2자 미만 → 400")
        void 닉네임_너무_짧음() throws Exception {
            UserMeRequest.UpdateProfile request = UserMeRequest.UpdateProfile.builder()
                    .nickname("a").build();

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/me/profile")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("닉네임은 2~20자로 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 닉네임 20자 초과 → 400")
        void 닉네임_너무_김() throws Exception {
            UserMeRequest.UpdateProfile request = UserMeRequest.UpdateProfile.builder()
                    .nickname("a".repeat(21)).build();

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/me/profile")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("닉네임은 2~20자로 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 포지션 50자 초과 → 400")
        void 포지션_너무_김() throws Exception {
            UserMeRequest.UpdateProfile request = UserMeRequest.UpdateProfile.builder()
                    .position("a".repeat(51)).build();

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/me/profile")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("포지션은 50자 이내로 입력해주세요."));
        }

        @Test
        @DisplayName("실패 - 소개 150자 초과 → 400")
        void 소개_너무_김() throws Exception {
            UserMeRequest.UpdateProfile request = UserMeRequest.UpdateProfile.builder()
                    .about("a".repeat(151)).build();

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/me/profile")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("소개는 150자 이내로 입력해주세요."));
        }
    }

    private MockMultipartFile toMultipartJson(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name, "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value)
        );
    }
}
