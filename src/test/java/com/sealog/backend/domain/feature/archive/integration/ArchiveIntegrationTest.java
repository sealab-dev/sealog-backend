package com.sealog.backend.domain.feature.archive.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.archive.dto.ArchiveRequest;
import com.sealog.backend.domain.feature.archive.entity.Archive;
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
 * Archive 통합 테스트
 */
@DisplayName("Archive 통합 테스트 (Controller → Service → Repository)")
class ArchiveIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;
    private User otherUser;
    private CustomUserDetails myDetails;    // testUser 인증 컨텍스트
    private CustomUserDetails otherDetails; // otherUser 인증 컨텍스트

    private Archive testArchive;    // testUser 소유, 공개
    private Archive privateArchive; // testUser 소유, 비공개
    private Archive otherArchive;   // otherUser 소유, 공개

    private Post publishedPost;  // testArchive 소속, PUBLISHED
    private Post unassignedPost; // 아카이브 미배정, PUBLISHED (배정 테스트용)
    private Post otherPost;      // otherUser 소유, PUBLISHED

    @BeforeEach
    void setUp() {
        // 사용자 생성
        testUser  = testDataFactory.createUser(UserRole.USER);
        otherUser = testDataFactory.createUser(UserRole.USER);

        // Mock 인증 컨텍스트 생성 (JWT 필터 우회)
        myDetails    = new CustomUserDetails(testUser);
        otherDetails = new CustomUserDetails(otherUser);

        // 아카이브 생성
        testArchive    = testDataFactory.createArchive(testUser,  true);
        privateArchive = testDataFactory.createArchive(testUser,  false);
        otherArchive   = testDataFactory.createArchive(otherUser, true);

        // 게시글 생성
        publishedPost  = testDataFactory.createPost(testUser, testArchive, PostStatus.PUBLISHED);
        unassignedPost = testDataFactory.createPost(testUser,              PostStatus.PUBLISHED);
        otherPost      = testDataFactory.createPost(otherUser,             PostStatus.PUBLISHED);
    }

    @Test
    @Order(0)
    void warmUp() {
        // 아무것도 안 함, JVM 웜업용
    }

    // =====================================================================
    // 공개 아카이브 목록 조회 (Guest)
    // =====================================================================
    @Nested
    @DisplayName("공개 아카이브 목록 조회 (Guest GET /{nickname})")
    class 공개_아카이브_목록_조회 {

        @Test
        @DisplayName("성공 - 공개 아카이브만 반환 → 200, 비공개 미포함")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/guest/archive/{nickname}", testUser.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].slug").value(testArchive.getSlug()));
        }
    }

    // =====================================================================
    // 아카이브 내 공개 게시글 조회 (Guest)
    // =====================================================================
    @Nested
    @DisplayName("아카이브 내 공개 게시글 조회 (Guest GET /{archiveId}/posts)")
    class 아카이브_내_공개_게시글_조회 {

        @Test
        @DisplayName("성공 - PUBLISHED 게시글 반환 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/guest/archive/{archiveId}/posts", testArchive.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].slug").value(publishedPost.getSlug()));
        }
    }

    // =====================================================================
    // 내 아카이브 목록 조회 (User)
    // =====================================================================
    @Nested
    @DisplayName("내 아카이브 목록 조회 (User GET)")
    class 내_아카이브_목록_조회 {

        @Test
        @DisplayName("성공 - 공개·비공개 모두 반환 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/user/archive")
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("실패 - 미인증 → 401")
        void 미인증() throws Exception {
            mockMvc.perform(get("/api/user/archive")
                            .with(anonymous()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =====================================================================
    // 내 아카이브 게시글 목록 조회 (User)
    // =====================================================================
    @Nested
    @DisplayName("내 아카이브 게시글 목록 조회 (User GET /{archiveId}/posts)")
    class 내_아카이브_게시글_목록_조회 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(get("/api/user/archive/{archiveId}/posts", testArchive.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("실패 - 미인증 → 401")
        void 미인증() throws Exception {
            mockMvc.perform(get("/api/user/archive/{archiveId}/posts", testArchive.getId())
                            .with(anonymous()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =====================================================================
    // 아카이브 생성
    // =====================================================================
    @Nested
    @DisplayName("아카이브 생성 (POST)")
    class 아카이브_생성 {

        @Test
        @DisplayName("성공 → 201")
        void 성공() throws Exception {
            ArchiveRequest.Create request = ArchiveRequest.Create.builder()
                    .name("새로운 아카이브")
                    .build();

            mockMvc.perform(post("/api/user/archive")
                            .with(user(myDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 이름 중복 → 400")
        void 이름_중복() throws Exception {
            ArchiveRequest.Create request = ArchiveRequest.Create.builder()
                    .name(testArchive.getName())
                    .build();

            mockMvc.perform(post("/api/user/archive")
                            .with(user(myDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 미인증 → 401")
        void 미인증() throws Exception {
            ArchiveRequest.Create request = ArchiveRequest.Create.builder()
                    .name("새로운 아카이브")
                    .build();

            mockMvc.perform(post("/api/user/archive")
                            .with(anonymous())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =====================================================================
    // 아카이브 이름 수정
    // =====================================================================
    @Nested
    @DisplayName("아카이브 이름 수정 (PUT /{nickname}/{slug})")
    class 아카이브_이름_수정 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            ArchiveRequest.Update request = ArchiveRequest.Update.builder()
                    .name("수정된 이름")
                    .build();

            mockMvc.perform(put("/api/user/archive/{nickname}/{slug}", testUser.getNickname(), testArchive.getSlug())
                            .with(user(myDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 같은 이름으로 수정 → 400")
        void 같은_이름() throws Exception {
            ArchiveRequest.Update request = ArchiveRequest.Update.builder()
                    .name(testArchive.getName())
                    .build();

            mockMvc.perform(put("/api/user/archive/{nickname}/{slug}", testUser.getNickname(), testArchive.getSlug())
                            .with(user(myDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 다른 아카이브와 이름 중복 → 409")
        void 이름_중복() throws Exception {
            ArchiveRequest.Update request = ArchiveRequest.Update.builder()
                    .name(privateArchive.getName())
                    .build();

            mockMvc.perform(put("/api/user/archive/{nickname}/{slug}", testUser.getNickname(), testArchive.getSlug())
                            .with(user(myDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패 - 타인 아카이브 수정 → 403")
        void 권한_없음() throws Exception {
            ArchiveRequest.Update request = ArchiveRequest.Update.builder()
                    .name("수정된 이름")
                    .build();

            mockMvc.perform(put("/api/user/archive/{nickname}/{slug}", testUser.getNickname(), testArchive.getSlug())
                            .with(user(otherDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // =====================================================================
    // 아카이브 공개
    // =====================================================================
    @Nested
    @DisplayName("아카이브 공개 (PATCH /{nickname}/{slug}/show)")
    class 아카이브_공개 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{nickname}/{slug}/show",
                            testUser.getNickname(), privateArchive.getSlug())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 아카이브 → 403")
        void 권한_없음() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{nickname}/{slug}/show",
                            testUser.getNickname(), testArchive.getSlug())
                            .with(user(otherDetails)))
                    .andExpect(status().isForbidden());
        }
    }

    // =====================================================================
    // 아카이브 비공개
    // =====================================================================
    @Nested
    @DisplayName("아카이브 비공개 (PATCH /{nickname}/{slug}/hide)")
    class 아카이브_비공개 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{nickname}/{slug}/hide",
                            testUser.getNickname(), testArchive.getSlug())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 아카이브 → 403")
        void 권한_없음() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{nickname}/{slug}/hide",
                            testUser.getNickname(), testArchive.getSlug())
                            .with(user(otherDetails)))
                    .andExpect(status().isForbidden());
        }
    }

    // =====================================================================
    // 아카이브 삭제
    // =====================================================================
    @Nested
    @DisplayName("아카이브 삭제 (DELETE /{nickname}/{slug})")
    class 아카이브_삭제 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(delete("/api/user/archive/{nickname}/{slug}",
                            testUser.getNickname(), privateArchive.getSlug())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 아카이브 → 403")
        void 권한_없음() throws Exception {
            mockMvc.perform(delete("/api/user/archive/{nickname}/{slug}",
                            testUser.getNickname(), testArchive.getSlug())
                            .with(user(otherDetails)))
                    .andExpect(status().isForbidden());
        }
    }

    // =====================================================================
    // 게시글 아카이브 배정
    // =====================================================================
    @Nested
    @DisplayName("게시글 아카이브 배정 (PATCH /{archiveId}/post/{postId})")
    class 게시글_아카이브_배정 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{archiveId}/post/{postId}",
                            testArchive.getId(), unassignedPost.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 게시글 배정 시도 → 403")
        void 타인_게시글() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{archiveId}/post/{postId}",
                            testArchive.getId(), otherPost.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 타인 아카이브에 배정 시도 → 403")
        void 타인_아카이브() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{archiveId}/post/{postId}",
                            otherArchive.getId(), unassignedPost.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isForbidden());
        }
    }

    // =====================================================================
    // 게시글 아카이브 해제
    // =====================================================================
    @Nested
    @DisplayName("게시글 아카이브 해제 (DELETE /post/{postId})")
    class 게시글_아카이브_해제 {

        @Test
        @DisplayName("성공 → 200")
        void 성공() throws Exception {
            mockMvc.perform(delete("/api/user/archive/post/{postId}", publishedPost.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 게시글 해제 시도 → 403")
        void 타인_게시글() throws Exception {
            mockMvc.perform(delete("/api/user/archive/post/{postId}", otherPost.getId())
                            .with(user(myDetails)))
                    .andExpect(status().isForbidden());
        }
    }
}
