package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.file.service.FileMetadataService;
import com.sealog.backend.domain.feature.post.dto.PostRequest;
import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.post.util.PostMarkdownFileParser;
import com.sealog.backend.domain.feature.post.util.PostSlugGenerator;
import com.sealog.backend.domain.feature.post.util.PostValidateMarkdown;
import com.sealog.backend.domain.feature.stack.dto.StackResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostStackService postStackService;
    private final PostTagService postTagService;
    private final PostFileService postFileService;
    private final FileMetadataService fileMetadataService;

    @Override
    public Page<PostResponse.PostItems> getPosts(Pageable pageable) {
        return postRepository.findAllPublished(pageable)
                .map(this::buildPostItemsResponse);
    }

    @Override
    public Page<PostResponse.PostItems> getUserPosts(String nickname, Pageable pageable) {
        return postRepository.findPublishedByNickname(nickname, pageable)
                .map(this::buildPostItemsResponse);
    }

    @Override
    public PostResponse.Detail getDetail(String nickname, String slug) {
        Post post = postRepository.findPublishedByNicknameAndSlug(nickname, slug)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        return buildPostDetailResponse(post, true);
    }

    @Override
    public List<PostResponse.PostItems> autocomplete(String keyword) {
        return postRepository.findPublishedByKeyword(keyword, PageRequest.of(0, 10)).stream()
                .map(this::buildPostItemsResponse)
                .toList();
    }

    @Override
    public PostResponse.Edit getEdit(Long userId, String slug) {
        Post post = postRepository.findByUserIdAndSlug(userId, slug)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("접근 권한이 없습니다");
        }

        List<PostResponse.StackItem> stackItems = postStackService.getStackItemsByPostId(post.getId());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());

        return PostResponse.Edit.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getContent(),
                post.getStatus(),
                post.getThumbnailPath(),
                tagNames,
                stackItems,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Override
    public Page<PostResponse.PostItems> getDeleted(Long userId, Pageable pageable) {
        return postRepository.findDeletedPostsByUserId(userId, pageable)
                .map(this::buildPostItemsResponse);
    }

    @Override
    @Transactional
    public PostResponse.Detail create(User user, PostRequest.Create request) {
        validateTitleForCreate(user.getId(), request.getTitle());
        PostValidateMarkdown.validate(request.getContent());

        String slug = generateUniqueSlug(user.getId(), request.getTitle());

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .slug(slug)
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .status(PostStatus.PUBLISHED)
                .build();

        Post savedPost = postRepository.save(post);

        if (request.getStackIds() != null) {
            postStackService.updatePostStacks(savedPost.getId(), request.getStackIds());
        }

        if (request.getTags() != null) {
            postTagService.updatePostTags(savedPost.getId(), request.getTags());
        }

        if (request.getThumbnailFileId() != null) {
            handleThumbnailFromPreUpload(savedPost, request.getThumbnailFileId(), request.getThumbnailPath());
        }

        handleContentFilesFromMarkdown(savedPost.getId(), request.getContent());

        return buildPostDetailResponse(savedPost, false);
    }

    @Override
    @Transactional
    public PostResponse.Detail update(Long userId, Long postId, PostRequest.Update request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("접근 권한이 없습니다");
        }

        PostValidateMarkdown.validate(request.getContent());

        String newSlug = post.getSlug();
        if (!post.getTitle().equals(request.getTitle())) {
            validateTitleForUpdate(userId, postId, request.getTitle());
            newSlug = generateUniqueSlug(userId, request.getTitle());
            log.debug("게시글 수정 - 제목 변경으로 slug 재생성: postId={}, oldSlug={}, newSlug={}",
                    post.getId(), post.getSlug(), newSlug);
        }

        post.update(
                request.getTitle(),
                newSlug,
                request.getExcerpt(),
                request.getContent()
        );

        postStackService.updatePostStacks(post.getId(), request.getStackIds());
        postTagService.updatePostTags(post.getId(), request.getTags());

        handleThumbnailUpdate(post, request);
        handleContentFilesUpdate(post.getId(), request.getContent());

        return buildPostDetailResponse(post, false);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("접근 권한이 없습니다");
        }

        post.softDelete();

        log.info("게시글 소프트 삭제 완료: postId={}, slug={}, deletedAt={}",
                post.getId(), post.getSlug(), post.getDeletedAt());
    }

    @Override
    @Transactional
    public void restore(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("접근 권한이 없습니다");
        }

        if (!post.isDeleted()) {
            throw CustomException.badRequest("삭제되지 않은 게시글은 복구할 수 없습니다");
        }

        post.restoreFromDelete();

        log.info("게시글 복구 완료: postId={}, slug={}", post.getId(), post.getSlug());
    }

    // ========== private 메서드 ========== //

    // ========== DTO 빌더 ========== //

    private PostResponse.PostItems buildPostItemsResponse(Post post) {
        List<PostResponse.StackItem> stackItems = postStackService.getStackItemsByPostId(post.getId());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());

        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                post.getUser().getProfileImagePath()
        );

        return PostResponse.PostItems.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getStatus(),
                post.getThumbnailPath(),
                tagNames,
                stackItems,
                author,
                post.getCreatedAt()
        );
    }

    private PostResponse.Detail buildPostDetailResponse(Post post, boolean includeRelatedPosts) {
        List<PostResponse.StackItem> stackItems = postStackService.getStackItemsByPostId(post.getId());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());

        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                post.getUser().getProfileImagePath()
        );

        return PostResponse.Detail.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getContent(),
                post.getStatus(),
                post.getThumbnailPath(),
                tagNames,
                stackItems,
                author,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // ========== 파일 처리 ========== //

    private void handleContentFilesFromMarkdown(Long postId, String content) {
        Set<Long> fileIds = PostMarkdownFileParser.extractFileIds(content);

        if (fileIds.isEmpty()) {
            log.info("게시글 생성 - 본문에 파일 참조 없음: postId={}", postId);
            return;
        }

        log.info("게시글 생성 - 본문 파일 매핑 시작: postId={}, fileCount={}", postId, fileIds.size());
        fileMetadataService.validateFilesExist(new ArrayList<>(fileIds));
        postFileService.saveContentFiles(postId, new ArrayList<>(fileIds));
        log.info("게시글 생성 - 본문 파일 매핑 완료: postId={}", postId);
    }

    private void handleContentFilesUpdate(Long postId, String newContent) {
        Set<Long> oldFileIds = postFileService.getContentFileIds(postId);
        Set<Long> newFileIds = PostMarkdownFileParser.extractFileIds(newContent);

        Set<Long> fileIdsToDelete = new HashSet<>(oldFileIds);
        fileIdsToDelete.removeAll(newFileIds);

        Set<Long> fileIdsToAdd = new HashSet<>(newFileIds);
        fileIdsToAdd.removeAll(oldFileIds);

        log.info("게시글 수정 - 파일 매핑 변경 분석: postId={}, 기존={}, 신규={}, 삭제={}, 추가={}",
                postId, oldFileIds.size(), newFileIds.size(),
                fileIdsToDelete.size(), fileIdsToAdd.size());

        if (!fileIdsToDelete.isEmpty()) {
            postFileService.deleteContentFiles(postId, new ArrayList<>(fileIdsToDelete));
        }

        if (!fileIdsToAdd.isEmpty()) {
            fileMetadataService.validateFilesExist(new ArrayList<>(fileIdsToAdd));
            postFileService.saveContentFiles(postId, new ArrayList<>(fileIdsToAdd));
        }
    }

    private void handleThumbnailFromPreUpload(Post post, Long thumbnailFileId, String thumbnailPath) {
        log.info("게시글 생성 - 썸네일 처리 시작: postId={}, fileId={}", post.getId(), thumbnailFileId);
        fileMetadataService.validateFilesExist(List.of(thumbnailFileId));
        postFileService.saveThumbnail(post.getId(), thumbnailFileId);
        post.updateThumbnailPath(thumbnailPath);
        log.info("게시글 생성 - 썸네일 처리 완료: postId={}, path={}", post.getId(), thumbnailPath);
    }

    private void handleThumbnailUpdate(Post post, PostRequest.Update request) {
        if (request.getThumbnailFileId() != null) {
            fileMetadataService.validateFilesExist(List.of(request.getThumbnailFileId()));
            postFileService.deleteThumbnail(post.getId());
            postFileService.saveThumbnail(post.getId(), request.getThumbnailFileId());
            post.updateThumbnailPath(request.getThumbnailPath());
            return;
        }

        if (Boolean.TRUE.equals(request.getRemoveThumbnail())) {
            postFileService.deleteThumbnail(post.getId());
            post.removeThumbnailPath();
        }
    }

    // ========== Slug 생성 ========== //

    private String generateUniqueSlug(Long userId, String title) {
        String baseSlug = PostSlugGenerator.generate(title);

        if (!postRepository.existsByUserIdAndSlug(userId, baseSlug)) {
            return baseSlug;
        }

        for (int i = 2; i <= 100; i++) {
            String candidateSlug = PostSlugGenerator.generateWithSuffix(baseSlug, i);
            if (!postRepository.existsByUserIdAndSlug(userId, candidateSlug)) {
                log.info("Slug 중복으로 번호 추가: baseSlug={}, finalSlug={}", baseSlug, candidateSlug);
                return candidateSlug;
            }
        }

        String timestampSlug = baseSlug + "-" + System.currentTimeMillis();
        log.warn("Slug 중복 해소 실패, 타임스탬프 추가: {}", timestampSlug);
        return timestampSlug;
    }

    // ========== Validation ========== //

    private void validateTitleForCreate(Long userId, String title) {
        if (postRepository.existsByUserIdAndTitle(userId, title)) {
            throw CustomException.conflict("이미 사용 중인 제목입니다");
        }
    }

    private void validateTitleForUpdate(Long userId, Long postId, String newTitle) {
        postRepository.findByUserIdAndTitle(userId, newTitle).ifPresent(existingPost -> {
            if (!existingPost.getId().equals(postId)) {
                throw CustomException.conflict("이미 존재하는 제목입니다");
            }
        });
    }
}