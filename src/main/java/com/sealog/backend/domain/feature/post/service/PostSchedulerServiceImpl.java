package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 스케줄러 비즈니스 로직 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostSchedulerServiceImpl implements PostSchedulerService {

    private static final int CLEANUP_AFTER_DAYS = 7;

    private final PostRepository postRepository;
    private final PostFileService postFileService;
    private final PostCategoryService postCategoryService;
    private final PostTagService postTagService;

    /**
     * 만료된 소프트 딜리트 게시글 영구 삭제
     *
     * 처리 순서 (FK 위반 방지):
     * 1. 파일 매핑 삭제
     * 2. PostCategory 매핑 삭제
     * 3. PostTag 매핑 삭제
     * 4. 게시글 영구 삭제
     */
    @Override
    @Transactional
    public void cleanupExpiredPosts() {
        LocalDateTime deletedBefore = LocalDateTime.now().minusDays(CLEANUP_AFTER_DAYS);
        List<Post> postsToDelete = postRepository.findPostsToHardDelete(deletedBefore);

        if (postsToDelete.isEmpty()) {
            log.info("영구 삭제 대상 게시글 없음");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Post post : postsToDelete) {
            try {
                hardDelete(post);
                successCount++;
            } catch (Exception e) {
                log.error("게시글 영구 삭제 실패: postId={}, slug={}, error={}",
                        post.getId(), post.getSlug(), e.getMessage(), e);
                failCount++;
            }
        }

        log.info("게시글 정리 완료 - 성공: {}건, 실패: {}건", successCount, failCount);
    }

    // ========== Private Methods ========== //

    /**
     * 게시글 단건 영구 삭제
     */
    private void hardDelete(Post post) {
        postFileService.deleteAllMappings(post.getId());
        postCategoryService.deleteAllByPostId(post.getId());
        postTagService.deleteAllByPostId(post.getId());
        postRepository.delete(post);
    }
}
