package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("PostSchedulerService 단위 테스트")
class PostSchedulerServiceUnitTest extends UnitTest {

    @Mock private PostRepository postRepository;
    @Mock private PostFileService postFileService;
    @Mock private PostCategoryService postCategoryService;
    @Mock private PostTagService postTagService;

    @InjectMocks
    private PostSchedulerServiceImpl postSchedulerService;

    @Test
    @DisplayName("만료된 게시글 정리: 7일 이상 삭제된 게시글을 조회하여 영구 삭제를 수행한다.")
    void cleanupExpiredPosts_Success() {
        // given
        Post post1 = mock(Post.class);
        given(post1.getId()).willReturn(101L);
        
        Post post2 = mock(Post.class);
        given(post2.getId()).willReturn(102L);

        given(postRepository.findPostsToHardDelete(any(LocalDateTime.class)))
                .willReturn(List.of(post1, post2));

        // when
        postSchedulerService.cleanupExpiredPosts();

        // then
        // 101L 게시글 삭제 검증
        verify(postFileService).deleteAllMappings(101L);
        verify(postCategoryService).deleteAllByPostId(101L);
        verify(postTagService).deleteAllByPostId(101L);
        verify(postRepository).delete(post1);

        // 102L 게시글 삭제 검증
        verify(postFileService).deleteAllMappings(102L);
        verify(postCategoryService).deleteAllByPostId(102L);
        verify(postTagService).deleteAllByPostId(102L);
        verify(postRepository).delete(post2);
    }

    @Test
    @DisplayName("정리 대상 없음: 만료된 게시글이 없으면 아무 작업도 수행하지 않는다.")
    void cleanupExpiredPosts_NoTargets() {
        // given
        given(postRepository.findPostsToHardDelete(any(LocalDateTime.class)))
                .willReturn(List.of());

        // when
        postSchedulerService.cleanupExpiredPosts();

        // then
        verify(postRepository, never()).delete(any(Post.class));
        verify(postFileService, never()).deleteAllMappings(anyLong());
    }

    @Test
    @DisplayName("일부 실패: 한 게시글 삭제 중 예외가 발생해도 나머지 게시글은 계속 삭제된다.")
    void cleanupExpiredPosts_PartialFailure() {
        // given
        Post failPost = mock(Post.class);
        given(failPost.getId()).willReturn(101L);

        Post successPost = mock(Post.class);
        given(successPost.getId()).willReturn(102L);

        given(postRepository.findPostsToHardDelete(any(LocalDateTime.class)))
                .willReturn(List.of(failPost, successPost));

        // 101L 게시글 삭제 시 예외 발생
        doThrow(new RuntimeException("파일 삭제 실패")).when(postFileService).deleteAllMappings(101L);

        // when
        postSchedulerService.cleanupExpiredPosts();

        // then - 예외 게시글은 이후 단계가 수행되지 않음
        verify(postRepository, never()).delete(failPost);

        // 102L 게시글은 정상 삭제됨
        verify(postFileService).deleteAllMappings(102L);
        verify(postCategoryService).deleteAllByPostId(102L);
        verify(postTagService).deleteAllByPostId(102L);
        verify(postRepository).delete(successPost);
    }
}
