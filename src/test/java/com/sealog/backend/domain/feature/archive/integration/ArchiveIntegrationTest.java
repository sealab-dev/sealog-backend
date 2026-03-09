package com.sealog.backend.domain.feature.archive.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.archive.dto.ArchiveRequest;
import com.sealog.backend.domain.feature.archive.entity.Archive;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.security.jwt.JwtTokenProvider;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.base.TestIntegrationBaseV2;
import com.sealog.backend.support.component.TestDataFactory;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Archive 통합 테스트
 */
@DisplayName("Archive 통합 테스트 (Controller → Service → Repository)")
class ArchiveIntegrationTest extends TestIntegrationBaseV2 {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PostRepository postRepository;

    private User testUser;
    private User otherUser;
    private String myToken;
    private String otherToken;

    private Archive testArchive;
    private Archive privateArchive;
    private Archive otherArchive;

    private Post publishedPost;    // testArchive 소속, PUBLISHED
    private Post deletedPost;      // testArchive 소속, DELETED (Guest 쿼리 제외 검증용)
    private Post unassignedPost;   // 아카이브 미배정, PUBLISHED (배정 테스트용)
    private Post otherPost;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        testUser  = testDataFactory.createUser(UserRole.USER);
        otherUser = testDataFactory.createUser(UserRole.USER);

        // JWT 토큰 생성
        myToken    = jwtTokenProvider.createAccessToken(testUser.getId(),  testUser.getEmail());
        otherToken = jwtTokenProvider.createAccessToken(otherUser.getId(), otherUser.getEmail());

        // 아카이브 생성
        testArchive    = testDataFactory.createArchive(testUser,  true);   // 공개
        privateArchive = testDataFactory.createArchive(testUser,  false);  // 비공개
        otherArchive   = testDataFactory.createArchive(otherUser, true);   // 타인 소유, 공개

        // 게시글 생성
        publishedPost  = testDataFactory.createPost(testUser,  PostStatus.PUBLISHED);
        deletedPost    = testDataFactory.createPost(testUser,  PostStatus.DELETED);
        unassignedPost = testDataFactory.createPost(testUser,  PostStatus.PUBLISHED);
        otherPost      = testDataFactory.createPost(otherUser, PostStatus.PUBLISHED);

        // publishedPost, deletedPost를 testArchive에 배정
        publishedPost.addToArchive(testArchive);
        deletedPost.addToArchive(testArchive);
        postRepository.save(publishedPost);
        postRepository.save(deletedPost);
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
        @DisplayName("성공 - PUBLISHED 게시글만 반환 → 200, DELETED 미포함")
        void 성공() throws Exception {
            // testArchive: publishedPost(PUBLISHED) + deletedPost(DELETED) 총 2개
            // Guest는 PUBLISHED만 반환 → totalElements=1
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
                            .cookie(new Cookie("access_token", myToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("실패 - 미인증 → 401")
        void 미인증() throws Exception {
            mockMvc.perform(get("/api/user/archive"))
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
        @DisplayName("성공 - 게시 상태 무관 반환 → 200")
        void 성공() throws Exception {
            // testArchive: publishedPost(PUBLISHED) + deletedPost(DELETED) 총 2개
            // User는 상태 무관 반환 → totalElements=2
            mockMvc.perform(get("/api/user/archive/{archiveId}/posts", testArchive.getId())
                            .cookie(new Cookie("access_token", myToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("실패 - 미인증 → 401")
        void 미인증() throws Exception {
            mockMvc.perform(get("/api/user/archive/{archiveId}/posts", testArchive.getId()))
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
            ArchiveRequest.Add request = ArchiveRequest.Add.builder()
                    .name("새로운 아카이브")
                    .build();

            mockMvc.perform(post("/api/user/archive")
                            .cookie(new Cookie("access_token", myToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 이름 중복 → 400")
        void 이름_중복() throws Exception {
            ArchiveRequest.Add request = ArchiveRequest.Add.builder()
                    .name(testArchive.getName())
                    .build();

            mockMvc.perform(post("/api/user/archive")
                            .cookie(new Cookie("access_token", myToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 미인증 → 401")
        void 미인증() throws Exception {
            ArchiveRequest.Add request = ArchiveRequest.Add.builder()
                    .name("새로운 아카이브")
                    .build();

            mockMvc.perform(post("/api/user/archive")
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
            ArchiveRequest.Edit request = ArchiveRequest.Edit.builder()
                    .name("수정된 이름")
                    .build();

            mockMvc.perform(put("/api/user/archive/{nickname}/{slug}", testUser.getNickname(), testArchive.getSlug())
                            .cookie(new Cookie("access_token", myToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 같은 이름으로 수정 → 400")
        void 같은_이름() throws Exception {
            ArchiveRequest.Edit request = ArchiveRequest.Edit.builder()
                    .name(testArchive.getName())
                    .build();

            mockMvc.perform(put("/api/user/archive/{nickname}/{slug}", testUser.getNickname(), testArchive.getSlug())
                            .cookie(new Cookie("access_token", myToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 다른 아카이브와 이름 중복 → 409")
        void 이름_중복() throws Exception {
            // privateArchive와 같은 이름으로 testArchive 수정 시도
            ArchiveRequest.Edit request = ArchiveRequest.Edit.builder()
                    .name(privateArchive.getName())
                    .build();

            mockMvc.perform(put("/api/user/archive/{nickname}/{slug}", testUser.getNickname(), testArchive.getSlug())
                            .cookie(new Cookie("access_token", myToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패 - 타인 아카이브 수정 → 403")
        void 권한_없음() throws Exception {
            ArchiveRequest.Edit request = ArchiveRequest.Edit.builder()
                    .name("수정된 이름")
                    .build();

            mockMvc.perform(put("/api/user/archive/{nickname}/{slug}", testUser.getNickname(), testArchive.getSlug())
                            .cookie(new Cookie("access_token", otherToken))
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
                            .cookie(new Cookie("access_token", myToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 아카이브 → 403")
        void 권한_없음() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{nickname}/{slug}/show",
                            testUser.getNickname(), testArchive.getSlug())
                            .cookie(new Cookie("access_token", otherToken)))
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
                            .cookie(new Cookie("access_token", myToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 아카이브 → 403")
        void 권한_없음() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{nickname}/{slug}/hide",
                            testUser.getNickname(), testArchive.getSlug())
                            .cookie(new Cookie("access_token", otherToken)))
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
            // privateArchive는 게시글 미포함 → FK 제약 없이 삭제 가능
            mockMvc.perform(delete("/api/user/archive/{nickname}/{slug}",
                            testUser.getNickname(), privateArchive.getSlug())
                            .cookie(new Cookie("access_token", myToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 아카이브 → 403")
        void 권한_없음() throws Exception {
            mockMvc.perform(delete("/api/user/archive/{nickname}/{slug}",
                            testUser.getNickname(), testArchive.getSlug())
                            .cookie(new Cookie("access_token", otherToken)))
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
            // unassignedPost는 어느 아카이브에도 속하지 않은 상태
            mockMvc.perform(patch("/api/user/archive/{archiveId}/post/{postId}",
                            testArchive.getId(), unassignedPost.getId())
                            .cookie(new Cookie("access_token", myToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 게시글 배정 시도 → 403")
        void 타인_게시글() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{archiveId}/post/{postId}",
                            testArchive.getId(), otherPost.getId())
                            .cookie(new Cookie("access_token", myToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 타인 아카이브에 배정 시도 → 403")
        void 타인_아카이브() throws Exception {
            mockMvc.perform(patch("/api/user/archive/{archiveId}/post/{postId}",
                            otherArchive.getId(), unassignedPost.getId())
                            .cookie(new Cookie("access_token", myToken)))
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
            // publishedPost는 testArchive에 배정된 상태
            mockMvc.perform(delete("/api/user/archive/post/{postId}", publishedPost.getId())
                            .cookie(new Cookie("access_token", myToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 게시글 해제 시도 → 403")
        void 타인_게시글() throws Exception {
            mockMvc.perform(delete("/api/user/archive/post/{postId}", otherPost.getId())
                            .cookie(new Cookie("access_token", myToken)))
                    .andExpect(status().isForbidden());
        }
    }
}
