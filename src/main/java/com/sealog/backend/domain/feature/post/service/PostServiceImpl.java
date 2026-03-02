package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.file.service.FileMetadataService;
import com.sealog.backend.domain.feature.post.dto.PostRequest;
import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.dto.PostSearchCondition;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.post.repository.PostSpecification;
import com.sealog.backend.domain.feature.post.strategy.PostSearchStrategy;
import com.sealog.backend.domain.feature.post.util.PostMarkdownFileParser;
import com.sealog.backend.domain.feature.post.util.PostSlugGenerator;
import com.sealog.backend.domain.feature.post.util.PostValidateMarkdown;
import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.repository.StackRepository;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final StackRepository stackRepository;
    private final PostFileService postFileService;
    private final FileMetadataService fileMetadataService;
    private final PostSearchStrategy postSearchStrategy;

    // ========== Guest (공개) ========== //

    @Override
    public PostResponse.Detail getDetail(String nickname, String slug) {
        Post post = postRepository.findBySlugWithStacks(slug)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.getUser().getNickname().equals(nickname)) {
            throw CustomException.notFound("해당 사용자의 게시글이 아닙니다");
        }

        return buildPostDetailResponse(post, true);
    }

    @Override
    public Page<PostResponse.PostItems> search(PostSearchCondition condition, Pageable pageable) {
        return postRepository.findAll(PostSpecification.withCondition(condition), pageable)
                .map(this::buildPostItemsResponse);
    }

    @Override
    public List<PostResponse.PostItems> autocomplete(String keyword) {
        return postSearchStrategy.autocomplete(keyword, 5);
    }

    // ========== User (인증) ========== //

    @Override
    @Transactional
    public PostResponse.Detail create(User user, PostRequest.Create request) {
        validateTitleForCreate(request.getTitle());
        PostValidateMarkdown.validate(request.getContent());

        String slug = generateUniqueSlug(request.getTitle());

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .slug(slug)
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .status(PostStatus.PUBLISHED)
                .build();

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            post.updateTags(request.getTags());
        }

        if (request.getStacks() != null && !request.getStacks().isEmpty()) {
            List<Stack> stacks = stackRepository.findByNameIn(request.getStacks());
            post.updateStacks(new HashSet<>(stacks));
        }

        Post savedPost = postRepository.save(post);
        log.info("게시글 생성 완료: postId={}, slug={}", savedPost.getId(), savedPost.getSlug());

        if (request.getThumbnailFileId() != null) {
            handleThumbnailFromPreUpload(savedPost, request.getThumbnailFileId(), request.getThumbnailPath());
        }

        handleContentFilesFromMarkdown(savedPost.getId(), request.getContent());

        return buildPostDetailResponse(savedPost, false);
    }

    @Override
    public PostResponse.Edit getEdit(Long userId, String slug) {
        Post post = postRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        List<String> tags = post.getTags() != null
                ? new ArrayList<>(post.getTags())
                : new ArrayList<>();

        return PostResponse.Edit.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getContent(),
                post.getStatus(),
                post.getThumbnailPath(),
                tags,
                stackNames,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public PostResponse.Detail update(Long userId, String slug, PostRequest.Update request) {
        Post post = postRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        PostValidateMarkdown.validate(request.getContent());

        String newSlug = post.getSlug();
        if (!post.getTitle().equals(request.getTitle())) {
            validateTitleForUpdate(post.getId(), request.getTitle());
            newSlug = generateUniqueSlug(request.getTitle());
            log.info("게시글 수정 - 제목 변경으로 slug 재생성: postId={}, oldSlug={}, newSlug={}",
                    post.getId(), post.getSlug(), newSlug);
        }

        post.update(
                request.getTitle(),
                newSlug,
                request.getExcerpt(),
                request.getContent()
        );

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            post.updateTags(request.getTags());
        }

        if (request.getStacks() != null && !request.getStacks().isEmpty()) {
            List<Stack> stacks = stackRepository.findByNameIn(request.getStacks());
            post.updateStacks(new HashSet<>(stacks));
        }

        handleThumbnailUpdate(post, request);
        handleContentFilesUpdate(post.getId(), request.getContent());

        return buildPostDetailResponse(post, false);
    }

    @Override
    @Transactional
    public void delete(Long userId, String slug) {
        Post post = postRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        post.softDelete();

        log.info("게시글 소프트 삭제 완료: postId={}, slug={}, deletedAt={}",
                post.getId(), post.getSlug(), post.getDeletedAt());
    }

    @Override
    @Transactional
    public void restore(Long userId, String slug) {
        Post post = postRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (post.getStatus() != PostStatus.DELETED) {
            throw CustomException.badRequest("삭제되지 않은 게시글은 복구할 수 없습니다");
        }

        post.restoreFromDelete();

        log.info("게시글 복구 완료: postId={}, slug={}", post.getId(), post.getSlug());
    }

    @Override
    public Page<PostResponse.PostItems> search(Long userId, PostSearchCondition condition, Pageable pageable) {
        PostSearchCondition nonDeletedCondition = PostSearchCondition.builder()
                .stackName(condition.getStackName())
                .keyword(condition.getKeyword())
                .build();

        return postRepository.findAll(
                PostSpecification.withUserAndCondition(userId, nonDeletedCondition),
                pageable
        ).map(this::buildPostItemsResponse);
    }

    @Override
    public Page<PostResponse.PostItems> getDeleted(Long userId, Pageable pageable) {
        return postRepository.findDeletedPostsByUserId(userId, pageable)
                .map(this::buildPostItemsResponse);
    }

    // ========== DTO 빌더 메서드 ========== //

    private PostResponse.PostItems buildPostItemsResponse(Post post) {
        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        List<String> tags = post.getTags() != null
                ? new ArrayList<>(post.getTags())
                : new ArrayList<>();

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
                tags,
                stackNames,
                author,
                post.getCreatedAt()
        );
    }

    private PostResponse.Detail buildPostDetailResponse(Post post, boolean includeRelatedPosts) {
        List<String> stackNames = post.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        List<String> tags = post.getTags() != null
                ? new ArrayList<>(post.getTags())
                : new ArrayList<>();

        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                post.getUser().getProfileImagePath()
        );

        List<PostResponse.PostItems> relatedPostItems = includeRelatedPosts
                ? getRelatedPosts(post).stream().map(this::buildPostItemsResponse).collect(Collectors.toList())
                : List.of();

        return PostResponse.Detail.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getContent(),
                post.getStatus(),
                post.getThumbnailPath(),
                tags,
                stackNames,
                author,
                relatedPostItems,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // ========== 관련 게시글 추천 로직 ========== //

    private List<Post> getRelatedPosts(Post currentPost) {
        List<Post> relatedPosts = new ArrayList<>();

        List<String> stackNames = currentPost.getStacks().stream()
                .map(Stack::getName)
                .collect(Collectors.toList());

        if (stackNames.isEmpty()) {
            log.info("관련 게시글 조회 - Stack 없음, 최신 공개 게시글 조회: postId={}", currentPost.getId());
            return postRepository.findLatestPublicPosts(
                    currentPost.getId(),
                    PageRequest.of(0, 3)
            );
        }

        // 1순위: Stack 일치
        List<Post> firstPriority = postRepository.findRelatedPostsByStackAndType(
                currentPost.getId(),
                stackNames,
                PageRequest.of(0, 2)
        );

        relatedPosts.addAll(firstPriority);
        log.info("관련 게시글 조회 - 1순위: postId={}, count={}", currentPost.getId(), firstPriority.size());

        int remaining = 3 - relatedPosts.size();

        // 2순위: Stack 일치
        if (remaining > 0) {
            List<Post> secondPriority = postRepository.findRelatedPostsByStackOnly(
                    currentPost.getId(),
                    stackNames,
                    PageRequest.of(0, remaining)
            );

            relatedPosts.addAll(secondPriority);
            log.info("관련 게시글 조회 - 2순위: postId={}, count={}", currentPost.getId(), secondPriority.size());

            remaining = 3 - relatedPosts.size();
        }

        // 3순위: 최신 공개 게시글
        if (remaining > 0) {
            List<Post> latestPosts = postRepository.findLatestPublicPosts(
                    currentPost.getId(),
                    PageRequest.of(0, remaining)
            );

            List<Long> existingIds = relatedPosts.stream()
                    .map(Post::getId)
                    .toList();

            List<Post> filtered = latestPosts.stream()
                    .filter(p -> !existingIds.contains(p.getId()))
                    .limit(remaining)
                    .toList();

            relatedPosts.addAll(filtered);
            log.info("관련 게시글 조회 - 3순위: postId={}, count={}", currentPost.getId(), filtered.size());
        }

        log.info("관련 게시글 조회 완료: postId={}, totalCount={}", currentPost.getId(), relatedPosts.size());
        return relatedPosts;
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

    private void handleThumbnailFromPreUpload(Post post, Long thumbnailFileId, String thumbnailUrl) {
        log.info("게시글 생성 - 썸네일 처리 시작: postId={}, fileId={}", post.getId(), thumbnailFileId);
        fileMetadataService.validateFilesExist(List.of(thumbnailFileId));
        postFileService.saveThumbnail(post.getId(), thumbnailFileId);
        post.updateThumbnailUrl(thumbnailUrl);
        log.info("게시글 생성 - 썸네일 처리 완료: postId={}, path={}", post.getId(), thumbnailUrl);
    }

    private void handleThumbnailUpdate(Post post, PostRequest.Update request) {
        if (request.getThumbnailFileId() != null) {
            fileMetadataService.validateFilesExist(List.of(request.getThumbnailFileId()));
            postFileService.deleteThumbnail(post.getId());
            postFileService.saveThumbnail(post.getId(), request.getThumbnailFileId());
            post.updateThumbnailUrl(request.getThumbnailPath());
            return;
        }

        if (Boolean.TRUE.equals(request.getRemoveThumbnail())) {
            postFileService.deleteThumbnail(post.getId());
            post.removeThumbnail();
        }
    }

    // ========== Slug 생성 로직 ========== //

    private String generateUniqueSlug(String title) {
        String baseSlug = PostSlugGenerator.generate(title);

        if (!postRepository.existsBySlug(baseSlug)) {
            return baseSlug;
        }

        for (int i = 2; i <= 100; i++) {
            String candidateSlug = PostSlugGenerator.generateWithSuffix(baseSlug, i);
            if (!postRepository.existsBySlug(candidateSlug)) {
                log.info("Slug 중복으로 번호 추가: baseSlug={}, finalSlug={}", baseSlug, candidateSlug);
                return candidateSlug;
            }
        }

        String timestampSlug = baseSlug + "-" + System.currentTimeMillis();
        log.warn("Slug 중복 해소 실패, 타임스탬프 추가: {}", timestampSlug);
        return timestampSlug;
    }

    // ========== Validation ========== //

    private void validateTitleForCreate(String title) {
        if (postRepository.existsByTitle(title)) {
            throw CustomException.fieldError("title", "이미 사용 중인 제목입니다");
        }
    }

    private void validateTitleForUpdate(Long postId, String newTitle) {
        postRepository.findByTitle(newTitle).ifPresent(existingPost -> {
            if (!existingPost.getId().equals(postId)) {
                throw CustomException.conflict("이미 존재하는 제목입니다.");
            }
        });
    }
}
