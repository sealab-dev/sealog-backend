package com.sealog.backend.domain.feature.achieve.service;

import com.sealog.backend.domain.feature.achieve.dto.ArchivePostRequest;
import com.sealog.backend.domain.feature.achieve.dto.ArchivePostResponse;
import com.sealog.backend.domain.feature.achieve.entity.Archive;
import com.sealog.backend.domain.feature.achieve.entity.ArchivePost;
import com.sealog.backend.domain.feature.achieve.repository.ArchivePostRepository;
import com.sealog.backend.domain.feature.achieve.repository.ArchiveRepository;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ArchiveService 구현 클래스
 */
@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivePostServiceImpl implements ArchivePostService {

    // 사용 의존성
    private final ArchivePostRepository archivePostRepository;
    private final ArchiveRepository archiveRepository;
    private final PostRepository postRepository;

    @Override
    public Page<ArchivePostResponse.ArchivePostItems> getPagedPublicItems(Long postId, Pageable pageable) {

        return archivePostRepository
                .findByPostIdAndArchiveIsPublic(postId, true, pageable)
                .map(archivePost -> ArchivePostResponse.ArchivePostItems.of(
                        archivePost.getId(), archivePost.getSortOrder(), archivePost.getArchive().getName()
                ));
    }

    @Override
    public void add(Long userId, ArchivePostRequest.Add request) {

        // 1. 게시글 조회
        Post post = postRepository
                .findById(request.getPostId())
                .orElseThrow(() -> new CustomException("이미 삭제되었거나 존재하지 않는 글입니다.", HttpStatus.INTERNAL_SERVER_ERROR));

        // 2. 게시글 검증
        verifyPostOwner(post, userId); // 타인 게시글 접근 차단

        // 3. 아카이브 목록 조회
        List<Archive> archives = archiveRepository.findAllById(request.getArchiveIds());

        // 4. 아카이브 검증
        // 타인 접근 차단
        if (archives.stream().noneMatch(archive -> archive.isOwnedBy(userId)))
            throw new CustomException("다른 사용자의 아카이브를 추가할 수 없습니다.", HttpStatus.FORBIDDEN);

        // 5. 게시글 아카이브 추가
        List<ArchivePost> archivePosts = new ArrayList<>();

        for (int i = 0; i < archives.size(); i++) {
            archivePosts.add(
                    ArchivePost.builder()
                            .archive(archives.get(i))
                            .post(post)
                            .sortOrder(i)
                            .build()
            );
        }

        // 6. 저장
        archivePostRepository.saveAll(archivePosts);
    }


    @Override
    public void remove(Long userId, Long archivePostId) {

        // 1. 조회
        ArchivePost archivePost = archivePostRepository
                .findByIdWithPost(archivePostId)
                .orElseThrow(() -> new CustomException("이미 삭제되었거나 존재하지 않는 게시글 아카이브입니다.", HttpStatus.INTERNAL_SERVER_ERROR));

        // 2. 검증
        if (!archivePost.getPost().isWrittenBy(userId))
            throw new CustomException("다른 사용자의 글의 아카이브를 삭제할 수 없습니다.", HttpStatus.FORBIDDEN);

        // 3. 제거 수행
        archivePostRepository.delete(archivePost);
    }

    /**
     * 블로그 게시글 소유자 검증
     */
    private void verifyPostOwner(Post post, Long userId) {

        if (!post.isWrittenBy(userId))
            throw new CustomException("다른 사용자의 글의 아카이브에 접근할 수 없습니다.", HttpStatus.FORBIDDEN);
    }

}
