package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.domain.feature.series.service.SeriesService;
import com.sealog.backend.domain.feature.file.service.FileMetadataService;
import com.sealog.backend.domain.feature.post.dto.PostRequest;
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

        return postRepository
                .findPublishedByNicknameAndSlug(nickname, slug)
                .map(this::buildPostDetailResponse)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));
    }

    @Override
    public Page<PostResponse.PostItems> searchPosts(String nickname, String keyword, Pageable pageable) {
        Specification<Post> spec = PostCondition.search(nickname, keyword);
        return postRepository
                .findAll(spec, pageable)
                .map(this::buildPostItemsResponse);
    }

    @Override
    public PostResponse.Edit getEdit(Long userId, String slug) {
        Post post = postRepository.findByUserIdAndSlug(userId, slug)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        List<PostResponse.StackItem> stackItems = postStackService.getStackItemsByPostId(post.getId());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());
        String displayContent = PostHtmlParser.injectSrcAttributes(post.getContent(), fileStorageService.getBaseUrl());
        Long seriesId = Objects.nonNull(post.getSeries()) ? post.getSeries().getId() : null;

        return PostResponse.Edit.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                displayContent,
                post.getStatus(),
                fileStorageService.getFileUrl(post.getThumbnailPath()),
                tagNames,
                stackItems,
                seriesId,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Override
    public Page<PostResponse.PostItems> searchPostsByNickname(String nickname, String keyword, Pageable pageable) {
        Specification<Post> spec = PostCondition.searchByNickname(nickname, keyword);
        return postRepository
                .findAll(spec, pageable)
                .map(this::buildPostItemsResponse);
    }

    @Override
    public Page<PostResponse.PostItems> getPostsByStack(String nickname, String stackName, Pageable pageable) {
        return postRepository
                .findPublishedByNicknameAndStackName(nickname, stackName, pageable)
                .map(this::buildPostItemsResponse);
    }

    @Override
    public Page<PostResponse.PostItems> getDeleted(Long userId, Pageable pageable) {
        return postRepository
                .findDeletedPostsByUserId(userId, pageable)
                .map(this::buildPostItemsResponse);
    }

    // ========== 생성 / 수정 / 삭제 ========== //

    @Override
    @Transactional
    public PostResponse.Detail create(User user, PostRequest.Create request, MultipartFile thumbnail) {
        // 1. 유저별 제목 중복 확인
        validateTitle(user.getId(), request.getTitle());

        // 2. 본문 정제 (XSS → src 제거 → 유효하지 않은 파일 태그 제거)
        String refinedHtml = prepareContent(user.getId(), request.getContent());

        // 3. slug 생성
        String slug = generateUniqueSlug(user.getId(), request.getTitle());

        // 4. 시리즈 조회 (있는 경우)
        Series series = null;
        if (request.getSeriesId() != null) {
            series = seriesService.getByIdAndUserId(request.getSeriesId(), user.getId());
        }

        // 5. 게시글 저장
        Post savedPost = postRepository.save(Post.builder()
                .user(user)
                .title(request.getTitle())
                .slug(slug)
                .excerpt(request.getExcerpt())
                .content(refinedHtml)
                .status(PostStatus.PUBLISHED)
                .build());

        // 6. 시리즈 연결
        if (series != null) {
            savedPost.addToSeries(series);
        }

        // 7. 스택 / 태그 매핑
        if (request.getStackIds() != null) {
            postStackService.updatePostStacks(savedPost.getId(), request.getStackIds());
        }
        if (request.getTags() != null) {
            postTagService.updatePostTags(savedPost.getId(), request.getTags());
        }

        // 8. 썸네일 업로드 + 매핑
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String thumbnailPath = postFileService.saveThumbnailFile(savedPost.getId(), user, thumbnail);
            savedPost.updateThumbnailPath(thumbnailPath);
        }

        // 9. 본문 파일 매핑 (검증 완료된 refinedHtml 기준)
        postFileService.saveContentFileMappings(savedPost.getId(), refinedHtml);

        return buildPostDetailResponse(savedPost);
    }

    @Override
    @Transactional
    public PostResponse.Detail update(Long userId, Long postId, PostRequest.Update request, MultipartFile thumbnail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("접근 권한이 없습니다");
        }

        if (post.isDeleted()) {
            throw CustomException.badRequest("삭제된 게시글은 수정할 수 없습니다");
        }

        // 1. 본문 정제 (XSS → src 제거 → 유효하지 않은 파일 태그 제거)
        String finalContent = prepareContent(userId, request.getContent());

        // 2. slug 재생성 (제목 변경 시)
        String newSlug = post.getSlug();
        if (!post.getTitle().equals(request.getTitle())) {
            validateTitle(userId, postId, request.getTitle());
            newSlug = generateUniqueSlug(userId, request.getTitle());
            log.debug("게시글 수정 - 제목 변경으로 slug 재생성: postId={}, oldSlug={}, newSlug={}",
                    post.getId(), post.getSlug(), newSlug);
        }

        // 3. 시리즈 업데이트
        if (request.getSeriesId() != null) {
            Series series = seriesService.getByIdAndUserId(request.getSeriesId(), userId);
            post.addToSeries(series);
        } else {
            post.removeFromSeries();
        }

        // 4. 게시글 업데이트
        post.update(request.getTitle(), newSlug, request.getExcerpt(), finalContent);

        // 5. 스택 / 태그 매핑
        postStackService.updatePostStacks(post.getId(), request.getStackIds());
        postTagService.updatePostTags(post.getId(), request.getTags());

        // 6. 썸네일 업로드 + 매핑
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String thumbnailPath = postFileService.saveThumbnailFile(post.getId(), post.getUser(), thumbnail);
            post.updateThumbnailPath(thumbnailPath);
        }

        // 7. 본문 파일 매핑 증분 업데이트 (검증 완료된 finalContent 기준)
        postFileService.updateContentFileMappings(post.getId(), finalContent);

        return buildPostDetailResponse(post);
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

    // ========== DTO 빌더 ========== //

    /**
     * 게시글 목록 응답 DTO를 빌드합니다.
     * 썸네일 경로는 post.thumbnailPath(역정규화 컬럼)에서 직접 읽어
     * file_metadata 조인 없이 완성 URL을 조립합니다.
     */
    private PostResponse.PostItems buildPostItemsResponse(Post post) {
        List<PostResponse.StackItem> stackItems = postStackService.getStackItemsByPostId(post.getId());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());

        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                fileStorageService.getFileUrl(post.getUser().getProfileImagePath())
        );

        return PostResponse.PostItems.of(
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

    /**
     * 게시글 상세 응답 DTO를 빌드합니다.
     * DB에는 src 없이 저장된 본문을 조회 시점에 injectSrcAttributes로 완성 URL을 주입합니다.
     * 썸네일도 toFileUrl로 완성 URL을 조립해 반환합니다.
     */
    private PostResponse.Detail buildPostDetailResponse(Post post) {
        List<PostResponse.StackItem> stackItems = postStackService.getStackItemsByPostId(post.getId());
        List<String> tagNames = postTagService.getTagNamesByPostId(post.getId());

        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                fileStorageService.getFileUrl(post.getUser().getProfileImagePath())
        );

        String displayContent = PostHtmlParser.injectSrcAttributes(post.getContent(), fileStorageService.getBaseUrl());
        Long seriesId = Objects.nonNull(post.getSeries()) ? post.getSeries().getId() : null;
        String seriesSlug = Objects.nonNull(post.getSeries()) ? post.getSeries().getSlug() : null;
        String seriesName = Objects.nonNull(post.getSeries()) ? post.getSeries().getName() : null;

        return PostResponse.Detail.of(
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
                seriesId,
                seriesSlug,
                seriesName,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // ========== 본문 정제 ========== //

    /**
     * 본문을 저장 가능한 형태로 정제합니다.
     * 파싱은 단 1회만 수행하며, 이후 작업은 모두 동일한 Document 위에서 처리합니다.
     * 1. XSS 검증 + Safelist 정제 (PostHtmlSanitizer.sanitize)
     * 2. 파싱 + 파일 ID 추출 (PostHtmlParser.prepare) — 재파싱 없이 Document 보존
     * 3. 유효하지 않은 파일 ID 조회 (서비스 호출)
     * 4. src 제거 + 유효하지 않은 파일 태그 제거 후 직렬화 (finalize)
     */
    private String prepareContent(Long userId, String rawContent) {
        String sanitized = PostHtmlSanitizer.sanitize(rawContent);
        PostHtmlParser.ContentPrep prep = PostHtmlParser.prepare(sanitized);

        Set<Long> invalidIds = prep.getFileIds().isEmpty()
                ? Set.of()
                : fileMetadataService.findInvalidFileIds(new ArrayList<>(prep.getFileIds()), userId);

        return prep.finalize(invalidIds);
    }

    // ========== Slug 생성 ========== //

    /**
     * 사용자별 고유한 slug를 생성합니다.
     * 동일 사용자 내 slug 중복 시 "-2", "-3" 순으로 suffix를 붙여 재시도하며,
     * 100회 시도 후에도 중복이 해소되지 않으면 타임스탬프를 붙여 반환합니다.
     */
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

    /**
     * 게시글 생성 시 동일 사용자 내 제목 중복을 검증합니다.
     */
    private void validateTitle(Long userId, String title) {
        if (postRepository.existsByUserIdAndTitle(userId, title)) {
            throw CustomException.conflict("이미 사용 중인 제목입니다");
        }
    }

    /**
     * 게시글 수정 시 동일 사용자 내 제목 중복을 검증합니다.
     * 현재 수정 중인 게시글 자신은 중복 대상에서 제외합니다.
     */
    private void validateTitle(Long userId, Long postId, String newTitle) {
        postRepository.findByUserIdAndTitle(userId, newTitle).ifPresent(existingPost -> {
            if (!existingPost.getId().equals(postId)) {
                throw CustomException.conflict("이미 존재하는 제목입니다");
            }
        });
    }
}
