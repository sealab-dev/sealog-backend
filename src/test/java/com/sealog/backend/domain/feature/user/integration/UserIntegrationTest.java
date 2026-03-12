package com.sealog.backend.domain.feature.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.user.dto.UserRequest;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.SocialType;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("User 통합 테스트 (Controller → Service → Repository)")
class UserIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
    }

    // =========================================================
    // 내 프로필 조회
    // =========================================================

    @Nested
    @DisplayName("내 프로필 조회 GET /api/user/me/profile")
    class 내_프로필_조회 {

        @Test
        @DisplayName("성공 - 인증된 사용자 요청 → 200, 내 정보 반환")
        void 성공() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);

            // when & then
            mockMvc.perform(get("/api/user/me/profile")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                    .andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
                    .andExpect(jsonPath("$.data.nickname").value(testUser.getNickname()))
                    .andExpect(jsonPath("$.data.role").value("USER"))
                    .andExpect(jsonPath("$.data.socialLinks").isArray());
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청 → 401")
        void 인증_없음() throws Exception {
            mockMvc.perform(get("/api/user/me/profile"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================
    // 프로필 수정
    // =========================================================

    @Nested
    @DisplayName("프로필 수정 PATCH /api/user/me/profile")
    class 프로필_수정 {

        @Test
        @DisplayName("성공 - 닉네임 수정 → 200, 수정된 닉네임 반환")
        void 닉네임_수정_성공() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .nickname("새닉네임")
                    .build();

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                    .andExpect(jsonPath("$.message").value("프로필이 수정되었습니다"));
        }

        @Test
        @DisplayName("성공 - 포지션·소개 함께 수정 → 200, 수정된 정보 반환")
        void 포지션_소개_수정_성공() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .position("Backend Developer")
                    .about("Java/Spring 개발자입니다")
                    .build();

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.position").value("Backend Developer"))
                    .andExpect(jsonPath("$.data.about").value("Java/Spring 개발자입니다"));
        }

        @Test
        @DisplayName("성공 - 소셜 링크 포함 수정 → 200, 소셜 링크 반환")
        void 소셜_링크_수정_성공() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .socialLinks(List.of(
                            new UserRequest.UpdateSocialLink(SocialType.GITHUB, "https://github.com/testuser"),
                            new UserRequest.UpdateSocialLink(SocialType.LINKEDIN, "https://linkedin.com/in/testuser")
                    ))
                    .build();

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.socialLinks.length()").value(2));
        }

        @Test
        @DisplayName("성공 - socialLinks null → 소셜 링크 변경 없음 → 200")
        void 소셜_링크_null_변경없음() throws Exception {
            // given: 소셜 링크 먼저 저장
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdateProfile setupRequest = UserRequest.UpdateProfile.builder()
                    .socialLinks(List.of(
                            new UserRequest.UpdateSocialLink(SocialType.GITHUB, "https://github.com/testuser")
                    ))
                    .build();
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                    .file(toMultipartJson("request", setupRequest))
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails)));

            // when: socialLinks = null로 요청
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .nickname("새닉네임")
                    .build(); // socialLinks = null

            // then: 소셜 링크 변경 없이 1개 유지
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.socialLinks.length()").value(1));
        }

        @Test
        @DisplayName("성공 - socialLinks 빈 배열 → 전체 삭제 → 200")
        void 소셜_링크_빈배열_전체삭제() throws Exception {
            // given: 소셜 링크 먼저 저장
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdateProfile setupRequest = UserRequest.UpdateProfile.builder()
                    .socialLinks(List.of(
                            new UserRequest.UpdateSocialLink(SocialType.GITHUB, "https://github.com/testuser")
                    ))
                    .build();
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                    .file(toMultipartJson("request", setupRequest))
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails)));

            // when: socialLinks = [] 빈 배열
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .socialLinks(List.of())
                    .build();

            // then: 소셜 링크 전체 삭제 → 빈 배열 반환
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.socialLinks.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 이미 사용 중인 닉네임으로 수정 → 409")
        void 닉네임_중복() throws Exception {
            // given
            User anotherUser = testDataFactory.createUser(UserRole.USER);
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .nickname(anotherUser.getNickname())
                    .build();

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("성공 - 닉네임 없이 요청 → 200, 기존 닉네임 유지")
        void 닉네임_없음() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .build(); // nickname = null

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nickname").value(testUser.getNickname()));
        }

        @Test
        @DisplayName("실패 - 닉네임이 유효 길이(2~20자) 미만 → 400")
        void 닉네임_유효성_실패() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .nickname("a") // 1자 → 최솟값(2) 미만
                    .build();

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 중복 소셜 타입 → 400")
        void 중복_소셜_타입() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .socialLinks(List.of(
                            new UserRequest.UpdateSocialLink(SocialType.GITHUB, "https://github.com/user1"),
                            new UserRequest.UpdateSocialLink(SocialType.GITHUB, "https://github.com/user2") // 중복 타입
                    ))
                    .build();

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request))
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청 → 401")
        void 인증_없음() throws Exception {
            // given
            UserRequest.UpdateProfile request = UserRequest.UpdateProfile.builder()
                    .nickname("새닉네임")
                    .build();

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/me/profile")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================
    // 비밀번호 변경
    // =========================================================

    @Nested
    @DisplayName("비밀번호 변경 PATCH /api/user/me/password")
    class 비밀번호_변경 {

        @Test
        @DisplayName("성공 - 현재 비밀번호 확인 후 새 비밀번호로 변경 → 200")
        void 성공() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdatePassword request = UserRequest.UpdatePassword.builder()
                    .currentPassword("password")       // TestDataFactory 기본 패스워드
                    .newPassword("newPassword123")
                    .newPasswordConfirm("newPassword123")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/user/me/password")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다"));
        }

        @Test
        @DisplayName("실패 - 현재 비밀번호 불일치 → 400")
        void 현재_비밀번호_불일치() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdatePassword request = UserRequest.UpdatePassword.builder()
                    .currentPassword("wrongPassword")
                    .newPassword("newPassword123")
                    .newPasswordConfirm("newPassword123")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/user/me/password")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 새 비밀번호와 확인 비밀번호 불일치 → 400")
        void 새_비밀번호_확인_불일치() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdatePassword request = UserRequest.UpdatePassword.builder()
                    .currentPassword("password")
                    .newPassword("newPassword123")
                    .newPasswordConfirm("differentPassword")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/user/me/password")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 새 비밀번호가 현재 비밀번호와 동일 → 400")
        void 새_비밀번호_현재와_동일() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdatePassword request = UserRequest.UpdatePassword.builder()
                    .currentPassword("password")
                    .newPassword("password")           // 현재 비밀번호와 동일
                    .newPasswordConfirm("password")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/user/me/password")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 새 비밀번호가 유효 길이(8~20자) 미만 → 400")
        void 새_비밀번호_유효성_실패() throws Exception {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            UserRequest.UpdatePassword request = UserRequest.UpdatePassword.builder()
                    .currentPassword("password")
                    .newPassword("short")              // 8자 미만
                    .newPasswordConfirm("short")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/user/me/password")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청 → 401")
        void 인증_없음() throws Exception {
            // given
            UserRequest.UpdatePassword request = UserRequest.UpdatePassword.builder()
                    .currentPassword("password")
                    .newPassword("newPassword123")
                    .newPasswordConfirm("newPassword123")
                    .build();

            // when & then
            mockMvc.perform(patch("/api/user/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================
    // 공개 프로필 조회
    // =========================================================

    @Nested
    @DisplayName("공개 프로필 조회 GET /api/guest/user/{nickname}/profile")
    class 공개_프로필_조회 {

        @Test
        @DisplayName("성공 - 존재하는 닉네임 → 200, 공개 프로필 반환")
        void 성공() throws Exception {
            // when & then
            mockMvc.perform(get("/api/guest/user/{nickname}/profile", testUser.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nickname").value(testUser.getNickname()))
                    .andExpect(jsonPath("$.data.socialLinks").isArray());
        }

        @Test
        @DisplayName("성공 - 응답에 민감 정보(email, password) 미포함")
        void 민감_정보_미포함() throws Exception {
            // when & then
            mockMvc.perform(get("/api/guest/user/{nickname}/profile", testUser.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").doesNotExist())
                    .andExpect(jsonPath("$.data.password").doesNotExist());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 닉네임 → 404")
        void 존재하지_않는_닉네임() throws Exception {
            // when & then
            mockMvc.perform(get("/api/guest/user/{nickname}/profile", "nobody"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================
    // 헬퍼 메서드
    // =========================================================

    /**
     * 객체를 JSON으로 직렬화하여 multipart 파트로 변환
     *
     * @param name  @RequestPart 파라미터명
     * @param value 직렬화할 객체
     */
    private MockMultipartFile toMultipartJson(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name, "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value)
        );
    }
}
