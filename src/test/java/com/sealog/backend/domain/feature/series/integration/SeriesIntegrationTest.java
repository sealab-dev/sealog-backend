package com.sealog.backend.domain.feature.series.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Series 통합 테스트
 */
@DisplayName("Series 통합 테스트 (Controller → Service → Repository)")
class SeriesIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;
    private User otherUser;
    private CustomUserDetails myDetails;    // testUser 인증 컨텍스트
    private CustomUserDetails otherDetails; // otherUser 인증 컨텍스트

    private Series testSeries;    // testUser 소유, 공개
    private Series privateSeries; // testUser 소유, 비공개
    private Series otherSeries;   // otherUser 소유, 공개

    private Post publishedPost;  // testSeries 소속, PUBLISHED
    private Post unassignedPost; // 시리즈 미배정, PUBLISHED
    private Post otherPost;      // otherUser 소유, PUBLISHED

    @BeforeEach
    void setUp() {
        // 사용자 생성
        testUser  = testDataFactory.createUser(UserRole.USER);
        otherUser = testDataFactory.createUser(UserRole.USER);

        // Mock 인증 컨텍스트 생성 (JWT 필터 우회)
        myDetails    = new CustomUserDetails(testUser);
        otherDetails = new CustomUserDetails(otherUser);

        // 시리즈 생성
        testSeries    = testDataFactory.createSeries(testUser,  true);
        privateSeries = testDataFactory.createSeries(testUser,  false);
        otherSeries   = testDataFactory.createSeries(otherUser, true);

        // 게시글 생성
        publishedPost  = testDataFactory.createPost(testUser, testSeries, PostStatus.PUBLISHED);
        unassignedPost = testDataFactory.createPost(testUser,              PostStatus.PUBLISHED);
        otherPost      = testDataFactory.createPost(otherUser,             PostStatus.PUBLISHED);
    }

    @Test
    @Order(0)
    void warmUp() {
        // 아무것도 안 함, JVM 웜업용
    }

    // =====================================================================
    // 공개 시리즈 목록 조회 (Guest)
    // =====================================================================
    @Nested
    @DisplayName("공개 시리즈 목록 조회 (Guest GET /api/{nickname}/series)")
    class 공개_시리즈_목록_조회 {

        @Test
        @DisplayName("성공 - 공개된 시리즈만 반환 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/{nickname}/series", testUser.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].slug").value(testSeries.getSlug()));
        }
    }

    // =====================================================================
    // 시리즈 내 공개 게시글 조회 (Guest)
    // =====================================================================
    @Nested
    @DisplayName("시리즈 내 공개 게시글 조회 (Guest GET /api/{nickname}/series/{slug})")
    class 시리즈_내_공개_게시글_조회 {

        @Test
        @DisplayName("성공 - PUBLISHED 게시글 반환 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/{nickname}/series/{slug}",
                            testUser.getNickname(), testSeries.getSlug()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].slug").value(publishedPost.getSlug()));
        }
    }

    // =====================================================================
    // 내 시리즈 목록 조회 (User)
    // =====================================================================
    @Nested
    @DisplayName("내 시리즈 목록 조회 (User GET /api/me/series)")
    class 내_시리즈_목록_조회 {

        @Test
        @DisplayName("성공 - 공개·비공개 모두 반환 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/me/series")
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("실패 - 미인증 → 401")
        void 미인증() throws Exception {
            mockMvc.perform(get("/api/me/series")
                            .with(anonymous()))
                    .andExpect(status().isUnauthorized());
            // 401은 Filter 수준에서 처리될 수 있어 CustomResponse가 안 나갈 수도 있음.
            // 하지만 GlobalExceptionHandler가 잡는 경우는 CustomResponse가 나감.
            // 일단 넘김.
        }
    }

    // =====================================================================
    // 내 시리즈 게시글 목록 조회 (User)
    // =====================================================================
    @Nested
    @DisplayName("내 시리즈 게시글 목록 조회 (User GET /api/me/series/{slug})")
    class 내_시리즈_게시글_목록_조회 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/me/series/{slug}", testSeries.getSlug())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("실패 - 타인 시리즈 접근 → 404")
        void 권한_없음() throws Exception {
            mockMvc.perform(get("/api/me/series/{slug}", testSeries.getSlug())
                            .with(user(otherDetails)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // =====================================================================
    // 시리즈 생성
    // =====================================================================
    @Nested
    @DisplayName("시리즈 생성 (POST /api/me/series)")
    class 시리즈_생성 {

        @Test
        @DisplayName("성공 → 201")
        void 성공() throws Exception {
            SeriesMeRequest.Create request = SeriesMeRequest.Create.builder()
                    .name("새로운 시리즈")
                    .build();

            mockMvc.perform(post("/api/me/series")
                            .with(user(myDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 이름 중복 → 400")
        void 이름_중복() throws Exception {
            SeriesMeRequest.Create request = SeriesMeRequest.Create.builder()
                    .name(testSeries.getName())
                    .build();

            mockMvc.perform(post("/api/me/series")
                            .with(user(myDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // =====================================================================
    // 시리즈 이름 수정
    // =====================================================================
    @Nested
    @DisplayName("시리즈 이름 수정 (PUT /api/me/series/{seriesId})")
    class 시리즈_이름_수정 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            SeriesMeRequest.Update request = SeriesMeRequest.Update.builder()
                    .name("수정된 이름")
                    .build();

            mockMvc.perform(put("/api/me/series/{seriesId}", testSeries.getId())
                            .with(user(myDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 시리즈 수정 → 403")
        void 권한_없음() throws Exception {
            SeriesMeRequest.Update request = SeriesMeRequest.Update.builder()
                    .name("수정된 이름")
                    .build();

            mockMvc.perform(put("/api/me/series/{seriesId}", otherSeries.getId())
                            .with(user(myDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // =====================================================================
    // 시리즈 공개
    // =====================================================================
    @Nested
    @DisplayName("시리즈 공개 (PATCH /api/me/series/{seriesId}/show)")
    class 시리즈_공개 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(patch("/api/me/series/{seriesId}/show", privateSeries.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // =====================================================================
    // 시리즈 비공개
    // =====================================================================
    @Nested
    @DisplayName("시리즈 비공개 (PATCH /api/me/series/{seriesId}/hide)")
    class 시리즈_비공개 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(patch("/api/me/series/{seriesId}/hide", testSeries.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // =====================================================================
    // 시리즈 삭제
    // =====================================================================
    @Nested
    @DisplayName("시리즈 삭제 (DELETE /api/me/series/{seriesId})")
    class 시리즈_삭제 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(delete("/api/me/series/{seriesId}", privateSeries.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
