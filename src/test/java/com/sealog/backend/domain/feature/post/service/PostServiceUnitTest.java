package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.domain.feature.series.service.SeriesService;
import com.sealog.backend.domain.feature.file.service.FileMetadataService;
import com.sealog.backend.domain.feature.post.dto.PostMeRequest;
import com.sealog.backend.domain.feature.post.dto.PostMeResponse;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.service.FileStorageService;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("Post 서비스 단위 테스트")
class PostServiceUnitTest extends UnitTest {

    @Mock private PostRepository postRepository;
    @Mock private SeriesService seriesService;
    @Mock private PostCategoryService postCategoryService;
    @Mock private PostTagService postTagService;
    @Mock private PostFileService postFileService;
    @Mock private FileMetadataService fileMetadataService;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private PostServiceImpl postService;

    @Nested
    @DisplayName("게시글 생성")
    class CreatePost {

        @Test
        @DisplayName("성공: 올바른 정보로 게시글을 생성할 수 있다.")
        void 생성_성공() {
            // given
            User user = mock(User.class);
            PostMeRequest.Create request = PostMeRequest.Create.builder()
                    .title("테스트 제목")
                    .content("<p>테스트 내용</p>")
                    .categoryIds(Collections.singletonList(1L))
                    .tags(Collections.singletonList("tag1"))
                    .build();

            Post post = Post.builder().user(user).title(request.getTitle()).build();
            ReflectionTestUtils.setField(post, "id", 100L);
            ReflectionTestUtils.setField(post, "slug", "테스트-제목");

            given(postRepository.save(any(Post.class))).willReturn(post);
            given(postTagService.getTagNamesByPostId(100L)).willReturn(Collections.singletonList("tag1"));
            given(postCategoryService.getPostCategoriesByPostId(100L)).willReturn(Collections.emptyList());

            // when
            PostMeResponse.MyPostItem result = postService.create(user, request, null);

            // then
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getTitle()).isEqualTo("테스트 제목");
            verify(postRepository).save(any(Post.class));
            verify(postCategoryService).savePostCategories(eq(100L), anyList());
            verify(postTagService).updatePostTags(eq(100L), anyList());
        }

        @Test
        @DisplayName("실패: 타인의 시리즈를 지정하여 생성할 경우 403 에러가 발생한다.")
        void 시리즈_권한_없음() {
            // given
            User user = mock(User.class);
            given(user.getId()).willReturn(1L);
            
            PostMeRequest.Create request = PostMeRequest.Create.builder()
                    .title("제목")
                    .content("내용")
                    .seriesId(50L)
                    .build();

            given(seriesService.getByIdAndUserId(50L, 1L))
                    .willThrow(CustomException.forbidden("권한이 없습니다."));

            // when & then
            assertThatThrownBy(() -> postService.create(user, request, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdatePost {

        @Test
        @DisplayName("성공: 작성자가 게시글을 수정할 수 있다.")
        void 수정_성공() {
            // given
            Long userId = 1L;
            Long postId = 10L;
            
            User owner = mock(User.class);
            given(owner.getId()).willReturn(userId);
            
            Post post = spy(Post.builder().user(owner).title("원본 제목").build());
            ReflectionTestUtils.setField(post, "id", postId);
            ReflectionTestUtils.setField(post, "slug", "원본-제목");

            PostMeRequest.Update request = PostMeRequest.Update.builder()
                    .title("수정 제목")
                    .content("수정 내용")
                    .build();

            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(postTagService.getTagNamesByPostId(postId)).willReturn(Collections.emptyList());
            given(postCategoryService.getPostCategoriesByPostId(postId)).willReturn(Collections.emptyList());

            // when
            postService.update(userId, postId, request, null);

            // then
            verify(post).update(eq("수정 제목"), anyString(), anyString(), eq("수정 내용"));
            verify(postCategoryService).savePostCategories(eq(postId), any());
            verify(postTagService).updatePostTags(eq(postId), any());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글인 경우 404 에러가 발생한다.")
        void 게시글_없음() {
            given(postRepository.findById(anyLong())).willReturn(Optional.empty());
            assertThatThrownBy(() -> postService.update(1L, 99L, PostMeRequest.Update.builder().build(), null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 작성자가 아닌 사용자가 수정을 시도할 경우 403 에러가 발생한다.")
        void 권한_없음() {
            User owner = mock(User.class);
            given(owner.getId()).willReturn(1L);
            Post post = Post.builder().user(owner).build();
            
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.update(2L, 10L, PostMeRequest.Update.builder().build(), null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("게시글 삭제 및 복구")
    class DeleteAndRestore {

        @Test
        @DisplayName("성공: 작성자가 게시글을 삭제(소프트 삭제)할 수 있다.")
        void 삭제_성공() {
            User owner = mock(User.class);
            given(owner.getId()).willReturn(1L);
            Post post = spy(Post.builder().user(owner).build());
            
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            postService.delete(1L, 10L);

            verify(post).softDelete();
        }

        @Test
        @DisplayName("실패: 작성자가 아닌 사용자가 삭제를 시도할 경우 403 에러가 발생한다.")
        void 삭제_권한_없음() {
            User owner = mock(User.class);
            given(owner.getId()).willReturn(1L);
            Post post = Post.builder().user(owner).build();

            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.delete(2L, 10L))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("성공: 삭제된 게시글을 복구할 수 있다.")
        void 복구_성공() {
            User owner = mock(User.class);
            given(owner.getId()).willReturn(1L);
            Post post = spy(Post.builder().user(owner).build());
            ReflectionTestUtils.setField(post, "deletedAt", java.time.LocalDateTime.now());
            
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            postService.restore(1L, 10L);

            verify(post).restoreFromDelete();
        }

        @Test
        @DisplayName("실패: 작성자가 아닌 사용자가 복구를 시도할 경우 403 에러가 발생한다.")
        void 복구_권한_없음() {
            User owner = mock(User.class);
            given(owner.getId()).willReturn(1L);
            Post post = Post.builder().user(owner).build();
            ReflectionTestUtils.setField(post, "deletedAt", java.time.LocalDateTime.now());

            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.restore(2L, 10L))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("실패: 삭제되지 않은 게시글을 복구하려고 할 때 400 에러가 발생한다.")
        void 복구_실패_삭제안됨() {
            User owner = mock(User.class);
            given(owner.getId()).willReturn(1L);
            Post post = Post.builder().user(owner).build();
            // deletedAt이 null인 상태
            
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.restore(1L, 10L))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                    .hasMessageContaining("삭제되지 않은 게시글입니다");
        }
    }

    @Nested
    @DisplayName("게시글 공개 상세 조회")
    class GetDetail {

        @Test
        @DisplayName("실패: 존재하지 않는 게시글이면 404 에러가 발생한다.")
        void 조회_실패_없는_게시글() {
            given(postRepository.findPublishedByNicknameAndSlug(anyString(), anyString()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getDetail("nickname", "not-exist"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("수정용 데이터 조회")
    class GetEdit {

        @Test
        @DisplayName("성공: 작성자가 수정용 데이터를 조회할 수 있다.")
        void 조회_성공() {
            // given
            Long userId = 1L;
            String slug = "test-post";
            Post post = Post.builder().title("제목").content("내용").status(PostStatus.PUBLISHED).build();
            ReflectionTestUtils.setField(post, "id", 10L);
            ReflectionTestUtils.setField(post, "slug", slug);

            given(postRepository.findByUserIdAndSlug(userId, slug)).willReturn(Optional.of(post));
            given(postCategoryService.getPostCategoriesByPostId(10L)).willReturn(Collections.emptyList());
            given(postTagService.getTagNamesByPostId(10L)).willReturn(Collections.emptyList());

            // when
            PostMeResponse.MyPostEdit result = postService.getEdit(userId, slug);

            // then
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getTitle()).isEqualTo("제목");
        }

        @Test
        @DisplayName("실패: 게시글이 없거나 작성자가 다르면 404 에러가 발생한다.")
        void 조회_실패() {
            given(postRepository.findByUserIdAndSlug(anyLong(), anyString())).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getEdit(1L, "none"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        }
    }
}
