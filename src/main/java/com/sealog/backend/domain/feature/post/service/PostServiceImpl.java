package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.domain.feature.series.service.SeriesService;
import com.sealog.backend.domain.feature.file.service.FileMetadataService;
import com.sealog.backend.domain.feature.post.dto.PostMeResponse;
import com.sealog.backend.domain.feature.post.dto.PostMeRequest;
import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.post.repository.condition.PostCondition;
import com.sealog.backend.domain.feature.post.util.PostHtmlParser;
import com.sealog.backend.domain.feature.post.util.PostHtmlSanitizer;
import com.sealog.backend.domain.feature.post.util.PostSlugGenerator;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final SeriesService seriesService;
    private final PostStackService postStackService;
    private final PostTagService postTagService;
    private final PostFileService postFileService;
    private final FileMetadataService fileMetadataService;
    private final FileStorageService fileStorageService;

    // ========== 조회 ========== //

    @Override
    public Page<PostResponse.PostItem> getPosts(Pageable pageable) {
        return postRepository.findAllPublished(pageable)
                .map(this::buildPostItemResponse);
    }

    @Override
    public Page<PostResponse.PostItem> getUserPosts(String nickname, Pageable pageable) {
        return postRepository.findPublishedByNickname(nickname, pageable)
                .map(this::buildPostItemResponse);
    }

    @Override
    public PostResponse.PostDetail getDetail(String nickname, String slug) {

        return postRepository
                .findPublishedByNicknameAndSlug(nickname, slug)
                .map(this::buildPostDetailResponse)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));
    }

    @Override
    public Page<PostResponse.PostItem> searchPosts(String nickname, String keyword, Pageable pageable) {
        Specification<Post> spec = PostCondition.search(nickname, keyword);
        return postRepository
                .findAll(spec, pageable)
                .map(this::buildPostItemResponse);
    }

    @Override
    public Page<PostMeResponse.MyPostItem> searchMyPosts(String nickname, String keyword, Pageable pageable) {
        Specification<Post> spec = PostCondition.search(nickname, keyword);
        return postRepository
                .findAll(spec, pageable)
                .map(this::buildPostMeItemResponse);
    }

    @Override
    public PostMeResponse.MyPostEdit getEdit(Long userId, String slug) {
        Post post = postRepository.findByUserIdAndSlug(userId, slug)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        List<PostMeResponse.MyStackItem> myStackItems = postStackService.getPostStacksByPostId(post.getId()).stream()
                .map(ps -> PostMeResponse.MyStackItem.of(ps.getStack().getId(), ps.getStack().getName(), ps.getSortOrder()))
                .collect(Collectors.toList());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());

        return PostMeResponse.MyPostEdit.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getContent(),
                post.getStatus(),
                fileStorageService.getFileUrl(post.getThumbnailPath()),
                tagNames,
                myStackItems,
                Objects.nonNull(post.getSeries()) ? post.getSeries().getId() : null,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Override
    public Page<PostResponse.PostItem> searchPostsByNickname(String nickname, String keyword, Pageable pageable) {
        Specification<Post> spec = PostCondition.searchByNickname(nickname, keyword);
        return postRepository
                .findAll(spec, pageable)
                .map(this::buildPostItemResponse);
    }

    @Override
    public Page<PostResponse.PostItem> getPostsByStack(String nickname, String stackName, Pageable pageable) {
        return postRepository
                .findPublishedByNicknameAndStackName(nickname, stackName, pageable)
                .map(this::buildPostItemResponse);
    }

    @Override
    public Page<PostMeResponse.MyPostItem> getDeleted(Long userId, Pageable pageable) {
        return postRepository
                .findDeletedPostsByUserId(userId, pageable)
                .map(this::buildPostMeItemResponse);
    }

    // ========== 생성 / 수정 / 삭제 ========== //

    @Override
    @Transactional
    public PostMeResponse.MyPostItem create(User user, PostMeRequest.Create request, MultipartFile thumbnail) {
        validateTitle(user.getId(), request.getTitle());
        String refinedHtml = prepareContent(user.getId(), request.getContent());
        String slug = generateUniqueSlug(user.getId(), request.getTitle());

        Series series = null;
        if (request.getSeriesId() != null) {
            series = seriesService.getByIdAndUserId(request.getSeriesId(), user.getId());
        }

        Post savedPost = postRepository.save(Post.builder()
                .user(user)
                .title(request.getTitle())
                .slug(slug)
                .excerpt(request.getExcerpt())
                .content(refinedHtml)
                .status(PostStatus.PUBLISHED)
                .build());

        if (series != null) {
            savedPost.addToSeries(series);
        }

        if (request.getStackIds() != null) {
            postStackService.updatePostStacks(savedPost.getId(), request.getStackIds());
        }
        if (request.getTags() != null) {
            postTagService.updatePostTags(savedPost.getId(), request.getTags());
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String thumbnailPath = postFileService.saveThumbnailFile(savedPost.getId(), user, thumbnail);
            savedPost.updateThumbnailPath(thumbnailPath);
        }

        postFileService.saveContentFileMappings(savedPost.getId(), refinedHtml);

        return buildPostMeItemResponse(savedPost);
    }

    @Override
    @Transactional
    public PostMeResponse.MyPostItem update(Long userId, Long postId, PostMeRequest.Update request, MultipartFile thumbnail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("접근 권한이 없습니다");
        }

        if (post.isDeleted()) {
            throw CustomException.badRequest("삭제된 게시글은 수정할 수 없습니다");
        }

        String finalContent = prepareContent(userId, request.getContent());

        String newSlug = post.getSlug();
        if (!post.getTitle().equals(request.getTitle())) {
            validateTitle(userId, postId, request.getTitle());
            newSlug = generateUniqueSlug(userId, request.getTitle());
        }

        if (request.getSeriesId() != null) {
            Series series = seriesService.getByIdAndUserId(request.getSeriesId(), userId);
            post.addToSeries(series);
        } else {
            post.removeFromSeries();
        }

        post.update(request.getTitle(), newSlug, request.getExcerpt(), finalContent);
        postStackService.updatePostStacks(post.getId(), request.getStackIds());
        postTagService.updatePostTags(post.getId(), request.getTags());

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String thumbnailPath = postFileService.saveThumbnailFile(post.getId(), post.getUser(), thumbnail);
            post.updateThumbnailPath(thumbnailPath);
        }

        postFileService.updateContentFileMappings(post.getId(), finalContent);

        return buildPostMeItemResponse(post);
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
    }

    // ========== DTO 빌더 ========== //

    private PostResponse.PostItem buildPostItemResponse(Post post) {
        List<PostResponse.StackItem> stackItems = postStackService.getPostStacksByPostId(post.getId()).stream()
                .map(ps -> PostResponse.StackItem.of(ps.getStack().getId(), ps.getStack().getName(), ps.getSortOrder()))
                .collect(Collectors.toList());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());

        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                fileStorageService.getFileUrl(post.getUser().getProfileImagePath())
        );

        return PostResponse.PostItem.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getStatus(),
                fileStorageService.getFileUrl(post.getThumbnailPath()),
                tagNames,
                stackItems,
                author,
                post.getCreatedAt()
        );
    }

    private PostMeResponse.MyPostItem buildPostMeItemResponse(Post post) {
        List<PostMeResponse.MyStackItem> myStackItems = postStackService.getPostStacksByPostId(post.getId()).stream()
                .map(ps -> PostMeResponse.MyStackItem.of(ps.getStack().getId(), ps.getStack().getName(), ps.getSortOrder()))
                .collect(Collectors.toList());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());

        return PostMeResponse.MyPostItem.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getStatus(),
                fileStorageService.getFileUrl(post.getThumbnailPath()),
                tagNames,
                myStackItems,
                post.getCreatedAt()
        );
    }

    private PostResponse.PostDetail buildPostDetailResponse(Post post) {
        PostResponse.SeriesInfo seriesInfo = Objects.nonNull(post.getSeries()) 
                ? PostResponse.SeriesInfo.of(post.getSeries().getId(), post.getSeries().getSlug(), post.getSeries().getName())
                : null;
        List<PostResponse.StackItem> stackItems = postStackService.getPostStacksByPostId(post.getId()).stream()
                .map(ps -> PostResponse.StackItem.of(ps.getStack().getId(), ps.getStack().getName(), ps.getSortOrder()))
                .collect(Collectors.toList());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());

        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                fileStorageService.getFileUrl(post.getUser().getProfileImagePath())
        );

        String displayContent = PostHtmlParser.injectSrcAttributes(post.getContent(), fileStorageService.getBaseUrl());
        return PostResponse.PostDetail.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                displayContent,
                post.getStatus(),
                fileStorageService.getFileUrl(post.getThumbnailPath()),
                tagNames,
                stackItems,
                author,
                seriesInfo,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // ========== 본문 정제 ========== //

    private String prepareContent(Long userId, String rawContent) {
        String sanitized = PostHtmlSanitizer.sanitize(rawContent);
        PostHtmlParser.ContentPrep prep = PostHtmlParser.prepare(sanitized);

        Set<Long> invalidIds = prep.getFileIds().isEmpty()
                ? Set.of()
                : fileMetadataService.findInvalidFileIds(new ArrayList<>(prep.getFileIds()), userId);

        return prep.finalize(invalidIds);
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
                return candidateSlug;
            }
        }

        return baseSlug + "-" + System.currentTimeMillis();
    }

    // ========== Validation ========== //

    private void validateTitle(Long userId, String title) {
        if (postRepository.existsByUserIdAndTitle(userId, title)) {
            throw CustomException.conflict("이미 사용 중인 제목입니다");
        }
    }

    private void validateTitle(Long userId, Long postId, String newTitle) {
        postRepository.findByUserIdAndTitle(userId, newTitle).ifPresent(existingPost -> {
            if (!existingPost.getId().equals(postId)) {
                throw CustomException.conflict("이미 존재하는 제목입니다");
            }
        });
    }
}
