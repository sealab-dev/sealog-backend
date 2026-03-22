package com.sealog.backend.domain.feature.post.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.category.entity.Category;
import com.sealog.backend.domain.feature.category.enums.CategoryGroup;
import com.sealog.backend.domain.feature.post.dto.PostMeRequest;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.entity.PostCategory;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostCategoryRepository;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.support.base.test.IntegrationTest;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 게시글 통합 테스트
 */
@DisplayName("Post 통합 테스트")
class PostIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;
    @Autowired PostRepository postRepository;
    @Autowired PostCategoryRepository postCategoryRepository;

    private User testUser;
    private CustomUserDetails userDetails;
    private Category testCategory;

    private Post publishedPost;
    private Post categoryPost;
    private Post draftPost;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createUser(UserRole.USER);
        userDetails = new CustomUserDetails(testUser);
        testCategory = testDataFactory.createCategory(CategoryGroup.LANGUAGE);

        publishedPost = testDataFactory.createPost(testUser, PostStatus.PUBLISHED, "일반 게시글");
        
        categoryPost = testDataFactory.createPost(testUser, PostStatus.PUBLISHED, "카테고리 게시글");
        postCategoryRepository.save(new PostCategory(categoryPost, testCategory, 0));

        draftPost = testDataFactory.createPost(testUser, PostStatus.DRAFT, "임시 저장글");
    }

    @Nested
    @DisplayName("공개 게시글 조회 (Guest)")
    class PublicPostInquiry {

        @Test
        @DisplayName("성공 - 전체 게시글 목록 조회")
        void getPosts_Success() throws Exception {
            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("성공 - 특정 사용자 게시글 목록 조회")
        void getUserPosts_Success() throws Exception {
            mockMvc.perform(get("/api/{nickname}/posts", testUser.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("성공 - 카테고리별 게시글 목록 조회")
        void getPostsByCategory_Success() throws Exception {
            mockMvc.perform(get("/api/{nickname}/posts", testUser.getNickname())
                    .param("categoryName", testCategory.getName()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value(categoryPost.getTitle()));
        }

        @Test
        @DisplayName("성공 - 게시글 상세 조회")
        void getPostDetail_Success() throws Exception {
            mockMvc.perform(get("/api/{nickname}/posts/{slug}", 
                    testUser.getNickname(), publishedPost.getSlug()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value(publishedPost.getTitle()));
        }

        @Test
        @DisplayName("성공 - 전체 게시글 키워드 검색")
        void searchPosts_Success() throws Exception {
            mockMvc.perform(get("/api/posts/search")
                    .param("keyword", "일반"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value(publishedPost.getTitle()));
        }

        @Test
        @DisplayName("성공 - 특정 사용자 게시글 키워드 검색")
        void searchPostsByNickname_Success() throws Exception {
            mockMvc.perform(get("/api/{nickname}/posts/search", testUser.getNickname())
                    .param("keyword", "카테고리"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value(categoryPost.getTitle()));
        }
    }

    @Nested
    @DisplayName("내 게시글 관리 (User)")
    class MyPostManagement {

        @Test
        @DisplayName("성공 - 게시글 생성")
        void createPost_Success() throws Exception {
            PostMeRequest.Create request = PostMeRequest.Create.builder()
                    .title("신규 게시글")
                    .content("<p>내용</p>")
                    .categoryIds(List.of(testCategory.getId()))
                    .build();

            MockMultipartFile jsonPart = new MockMultipartFile("request", "", 
                    MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

            mockMvc.perform(multipart("/api/me/posts")
                    .file(jsonPart)
                    .with(user(userDetails)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.title").value("신규 게시글"));
        }

        @Test
        @DisplayName("성공 - 게시글 수정")
        void updatePost_Success() throws Exception {
            PostMeRequest.Update request = PostMeRequest.Update.builder()
                    .title("수정된 제목")
                    .content("<p>수정된 본문</p>")
                    .build();

            MockMultipartFile jsonPart = new MockMultipartFile("request", "", 
                    MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

            mockMvc.perform(multipart(HttpMethod.PUT, "/api/me/posts/{postId}", publishedPost.getId())
                    .file(jsonPart)
                    .with(user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정된 제목"));
        }

        @Test
        @DisplayName("성공 - 게시글 삭제 및 복구")
        void deleteAndRestore_Success() throws Exception {
            mockMvc.perform(delete("/api/me/posts/{postId}", draftPost.getId()).with(user(userDetails)))
                    .andExpect(status().isOk());
            assertThat(postRepository.findById(draftPost.getId()).get().getDeletedAt()).isNotNull();

            mockMvc.perform(patch("/api/me/posts/{postId}/restore", draftPost.getId()).with(user(userDetails)))
                    .andExpect(status().isOk());
            assertThat(postRepository.findById(draftPost.getId()).get().getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - 내 게시글 검색 (DRAFT 포함)")
        void searchMyPosts_Success() throws Exception {
            mockMvc.perform(get("/api/me/posts/search")
                    .param("keyword", "임시")
                    .with(user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value(draftPost.getTitle()));
        }

        @Test
        @DisplayName("성공 - 삭제된 게시글 목록 조회")
        void getDeletedPosts_Success() throws Exception {
            mockMvc.perform(delete("/api/me/posts/{postId}", draftPost.getId()).with(user(userDetails)));

            mockMvc.perform(get("/api/me/posts/deleted").with(user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value(draftPost.getTitle()));
        }
    }
}
