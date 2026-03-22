package com.sealog.backend.domain.feature.series.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.support.base.test.IntegrationTest;
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
class SeriesIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;
    private CustomUserDetails myDetails;

    private Series testSeries;
    private Series privateSeries;
    private Post publishedPost;

    @BeforeEach
    void setUp() {
        testUser  = testDataFactory.createUser(UserRole.USER);
        myDetails = new CustomUserDetails(testUser);

        testSeries    = testDataFactory.createSeries(testUser, true);
        privateSeries = testDataFactory.createSeries(testUser, false);
        publishedPost = testDataFactory.createPost(testUser, testSeries, PostStatus.PUBLISHED);
    }

    @Nested
    @DisplayName("공개 시리즈 목록 조회 (Guest)")
    class 공개_시리즈_목록_조회 {

        @Test
        @DisplayName("성공 - 공개된 시리즈만 반환 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/{nickname}/series", testUser.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].slug").value(testSeries.getSlug()));
        }
    }

    @Nested
    @DisplayName("시리즈 내 공개 게시글 조회 (Guest)")
    class 시리즈_내_공개_게시글_조회 {

        @Test
        @DisplayName("성공 - PUBLISHED 게시글 반환 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/{nickname}/series/{slug}",
                            testUser.getNickname(), testSeries.getSlug()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].slug").value(publishedPost.getSlug()));
        }
    }

    @Nested
    @DisplayName("내 시리즈 목록 조회 (User)")
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
    }

    @Nested
    @DisplayName("내 시리즈 게시글 목록 조회 (User)")
    class 내_시리즈_게시글_목록_조회 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/me/series/{slug}", testSeries.getSlug())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

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
    }

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
    }

    @Nested
    @DisplayName("시리즈 공개/비공개 전환")
    class 시리즈_상태_전환 {

        @Test
        @DisplayName("성공 - 공개로 전환 → 200")
        void 공개_전환_성공() throws Exception {
            mockMvc.perform(patch("/api/me/series/{seriesId}/show", privateSeries.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("성공 - 비공개로 전환 → 200")
        void 비공개_전환_성공() throws Exception {
            mockMvc.perform(patch("/api/me/series/{seriesId}/hide", testSeries.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

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
