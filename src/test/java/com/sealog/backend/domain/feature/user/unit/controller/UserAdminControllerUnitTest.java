package com.sealog.backend.domain.feature.user.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.user.controller.UserAdminController;
import com.sealog.backend.domain.feature.user.dto.UserAdminRequest;
import com.sealog.backend.domain.feature.user.service.UserService;
import com.sealog.backend.global.exception.GlobalExceptionHandler;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UserAdminController 단위 테스트 - 유효성 검증")
class UserAdminControllerUnitTest extends UnitTest {

    @Mock UserService userService;
    @InjectMocks
    UserAdminController userAdminController;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userAdminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("사용자 생성 POST /api/admin/users - 유효성 검증 실패")
    class CreateUser {

        @Test
        @DisplayName("실패 - 이메일 공백 → 400")
        void 이메일_공백() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email("").password("password1").name("홍길동").nickname("tester").build();

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 이메일 형식 오류 → 400")
        void 이메일_형식_오류() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email("not-an-email").password("password1").name("홍길동").nickname("tester").build();

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("올바른 이메일 형식이 아닙니다"));
        }

        @Test
        @DisplayName("실패 - 비밀번호 8자 미만 → 400")
        void 비밀번호_너무_짧음() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email("test@local.com").password("short").name("홍길동").nickname("tester").build();

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("비밀번호는 8~20자로 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 비밀번호 20자 초과 → 400")
        void 비밀번호_너무_김() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email("test@local.com").password("a".repeat(21)).name("홍길동").nickname("tester").build();

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("비밀번호는 8~20자로 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 이름 2자 미만 → 400")
        void 이름_너무_짧음() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email("test@local.com").password("password1").name("홍").nickname("tester").build();

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이름은 2~20자로 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 닉네임 2자 미만 → 400")
        void 닉네임_너무_짧음() throws Exception {
            UserAdminRequest.Create request = UserAdminRequest.Create.builder()
                    .email("test@local.com").password("password1").name("홍길동").nickname("a").build();

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("닉네임은 2~20자로 입력해주세요"));
        }
    }
}
