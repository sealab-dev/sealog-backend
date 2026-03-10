package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.post.dto.PostRequest;
import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.post.util.PostHtmlParser;
import com.sealog.backend.domain.feature.post.util.PostHtmlSanitizer;
import com.sealog.backend.domain.feature.post.util.PostSlugGenerator;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostStackService postStackService;
    private final PostTagService postTagService;
    private final PostFileService postFileService;

    @Value("${storage.base-url}")
    private String storageBaseUrl;

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
        Post post = postRepository.findPublishedByNicknameAndSlug(nickname, slug)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        return buildPostDetailResponse(post);
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
        String displayContent = PostHtmlParser.injectSrcAttributes(post.getContent(), storageBaseUrl);

        return PostResponse.Edit.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                displayContent,
                post.getStatus(),
                toFileUrl(post.getThumbnailPath()),
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

    // ========== 생성 / 수정 / 삭제 ========== //

    @Override
    @Transactional
    public PostResponse.Detail create(User user, PostRequest.Create request, MultipartFile thumbnail) {
        // 유저별 제목 중복 확인
        validateTitle(user.getId(), request.getTitle());

        // XSS 방지 허용되지 않은 태그 일 경우 예외 반환
        // 본문 파싱을 통해 src를 제거(메타데이터 속성들만 남김)
        String processedContent = PostHtmlParser.stripSrcAttributes(
                PostHtmlSanitizer.sanitize(request.getContent())
        );

        // 제목으로 slug 생성
        String slug = generateUniqueSlug(user.getId(), request.getTitle());

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .slug(slug)
                .excerpt(request.getExcerpt())
                .content(processedContent)
                .status(PostStatus.PUBLISHED)
                .build();

        Post savedPost = postRepository.save(post);

        if (request.getStackIds() != null) {
            postStackService.updatePostStacks(savedPost.getId(), request.getStackIds());
        }

        if (request.getTags() != null) {
            postTagService.updatePostTags(savedPost.getId(), request.getTags());
        }

        // 썸네일이 있을 경우 파일을 저장하고 메타데이터 추가 및 post엔티티에 경로 추가
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String thumbnailPath = postFileService.saveThumbnailFile(savedPost.getId(), user, thumbnail);
            savedPost.updateThumbnailPath(thumbnailPath);
        }

        // 본문에서 파일을 추출해 매핑
        postFileService.saveContentFilesFromHtml(savedPost.getId(), user.getId(), processedContent);

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

        String processedContent = PostHtmlParser.stripSrcAttributes(
                PostHtmlSanitizer.sanitize(request.getContent())
        );

        String newSlug = post.getSlug();
        if (!post.getTitle().equals(request.getTitle())) {
            validateTitle(userId, postId, request.getTitle());
            newSlug = generateUniqueSlug(userId, request.getTitle());
            log.debug("게시글 수정 - 제목 변경으로 slug 재생성: postId={}, oldSlug={}, newSlug={}",
                    post.getId(), post.getSlug(), newSlug);
        }

        post.update(request.getTitle(), newSlug, request.getExcerpt(), processedContent);

        postStackService.updatePostStacks(post.getId(), request.getStackIds());
        postTagService.updatePostTags(post.getId(), request.getTags());

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String thumbnailPath = postFileService.saveThumbnailFile(post.getId(), post.getUser(), thumbnail);
            post.updateThumbnailPath(thumbnailPath);
        }

        postFileService.updateContentFilesFromHtml(post.getId(), userId, processedContent);

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
                toFileUrl(post.getUser().getProfileImagePath())
        );

        return PostResponse.PostItems.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getStatus(),
                toFileUrl(post.getThumbnailPath()),
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
                toFileUrl(post.getUser().getProfileImagePath())
        );

        String displayContent = PostHtmlParser.injectSrcAttributes(post.getContent(), storageBaseUrl);

        return PostResponse.Detail.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                displayContent,
                post.getStatus(),
                toFileUrl(post.getThumbnailPath()),
                tagNames,
                stackItems,
                author,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    // ========== URL 조립 ========== //

    private String toFileUrl(String path) {
        if (path == null) {
            return null;
        }
        String base = storageBaseUrl.endsWith("/")
                ? storageBaseUrl.substring(0, storageBaseUrl.length() - 1)
                : storageBaseUrl;
        return path.startsWith("/") ? base + path : base + "/" + path;
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