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
import com.sealog.backend.domain.feature.post.util.PostSlugGenerator;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.category.dto.CategoryResponse;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시글 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final SeriesService seriesService;
    private final PostCategoryService postCategoryService;
    private final PostTagService postTagService;
    private final PostFileService postFileService;
    private final FileStorageService fileStorageService;

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
        return postRepository.findPublishedByNicknameAndSlug(nickname, slug)
                .map(this::buildPostDetailResponse)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));
    }

    @Override
    public Page<PostResponse.PostItem> searchPosts(String nickname, String keyword, Pageable pageable) {
        Specification<Post> spec = PostCondition.search(nickname, keyword);
        return postRepository.findAll(spec, pageable)
                .map(this::buildPostItemResponse);
    }

    @Override
    public Page<PostMeResponse.MyPostItem> searchMyPosts(String nickname, String keyword, Pageable pageable) {
        Specification<Post> spec = PostCondition.search(nickname, keyword);
        return postRepository.findAll(spec, pageable)
                .map(this::buildPostMeItemResponse);
    }

    @Override
    public Page<PostResponse.PostItem> searchPostsByNickname(String nickname, String keyword, Pageable pageable) {
        return postRepository.findPublishedByNickname(nickname, pageable)
                .map(this::buildPostItemResponse);
    }

    @Override
    public Page<PostResponse.PostItem> getPostsByCategory(String nickname, String categoryName, Pageable pageable) {
        return postRepository.findPublishedByNicknameAndCategoryName(nickname, categoryName, pageable)
                .map(this::buildPostItemResponse);
    }

    @Override
    public PostMeResponse.MyPostEdit getEdit(Long userId, String slug) {
        Post post = postRepository.findByUserIdAndSlug(userId, slug)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        List<PostMeResponse.MyCategoryItem> myCategoryItems = postCategoryService.getPostCategoriesByPostId(post.getId()).stream()
                .map(pc -> PostMeResponse.MyCategoryItem.of(pc.getCategory().getId(), pc.getCategory().getName(), pc.getSortOrder()))
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
                post.getSeries() != null ? post.getSeries().getId() : null,
                myCategoryItems,
                tagNames
        );
    }

    @Override
    public Page<PostMeResponse.MyPostItem> getDeleted(Long userId, Pageable pageable) {
        return postRepository.findDeletedPostsByUserId(userId, pageable)
                .map(this::buildPostMeItemResponse);
    }

    @Override
    @Transactional
    public PostMeResponse.MyPostItem create(User user, PostMeRequest.Create request, MultipartFile thumbnail) {
        String thumbnailPath = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            // TODO: 파일 업로드 연동
        }

        String slug = PostSlugGenerator.generate(request.getTitle());
        String excerpt = PostHtmlParser.extractExcerpt(request.getContent());

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .slug(slug)
                .excerpt(excerpt)
                .content(request.getContent())
                .status(PostStatus.PUBLISHED)
                .thumbnailPath(thumbnailPath)
                .build();

        if (request.getSeriesId() != null) {
            Series series = seriesService.getByIdAndUserId(request.getSeriesId(), user.getId());
            post.addToSeries(series);
        }

        Post savedPost = postRepository.save(post);

        postCategoryService.savePostCategories(savedPost.getId(), request.getCategoryIds());
        postTagService.updatePostTags(savedPost.getId(), request.getTags());

        return buildPostMeItemResponse(savedPost);
    }

    @Override
    @Transactional
    public PostMeResponse.MyPostItem update(Long userId, Long postId, PostMeRequest.Update request, MultipartFile thumbnail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }

        String slug = PostSlugGenerator.generate(request.getTitle());
        String excerpt = PostHtmlParser.extractExcerpt(request.getContent());

        post.update(request.getTitle(), slug, excerpt, request.getContent());

        if (request.getSeriesId() != null) {
            Series series = seriesService.getByIdAndUserId(request.getSeriesId(), userId);
            post.addToSeries(series);
        } else {
            post.removeFromSeries();
        }

        postCategoryService.savePostCategories(postId, request.getCategoryIds());
        postTagService.updatePostTags(postId, request.getTags());

        return buildPostMeItemResponse(post);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }

        post.softDelete();
    }

    @Override
    @Transactional
    public void restore(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.isWrittenBy(userId)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }

        post.restoreFromDelete();
    }

    @Override
    @Transactional
    public String uploadImage(MultipartFile file, Long userId) {
        return null;
    }

    // ========== Private 보조 메소드 (DTO 변환 및 로직 캡슐화) ========== //

    /**
     * 공개용 게시글 목록 항목 DTO를 생성합니다.
     */
    private PostResponse.PostItem buildPostItemResponse(Post post) {
        List<String> tags = postTagService.getTagNamesByPostId(post.getId());
        List<PostResponse.CategoryItem> categories = postCategoryService.getCategoryItemsByPostId(post.getId());
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
                tags,
                categories,
                author,
                post.getCreatedAt()
        );
    }

    /**
     * 공개용 게시글 상세 정보 DTO를 생성합니다.
     */
    private PostResponse.PostDetail buildPostDetailResponse(Post post) {
        List<String> tags = postTagService.getTagNamesByPostId(post.getId());
        List<PostResponse.CategoryItem> categories = postCategoryService.getCategoryItemsByPostId(post.getId());
        PostResponse.AuthorInfo author = PostResponse.AuthorInfo.of(
                post.getUser().getNickname(),
                fileStorageService.getFileUrl(post.getUser().getProfileImagePath())
        );
        PostResponse.SeriesInfo seriesInfo = post.getSeries() != null ?
                PostResponse.SeriesInfo.of(post.getSeries().getId(), post.getSeries().getSlug(), post.getSeries().getName()) : null;

        return PostResponse.PostDetail.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getContent(),
                post.getStatus(),
                fileStorageService.getFileUrl(post.getThumbnailPath()),
                tags,
                categories,
                author,
                seriesInfo,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    /**
     * 인증된 사용자용 내 게시글 항목 DTO를 생성합니다.
     */
    private PostMeResponse.MyPostItem buildPostMeItemResponse(Post post) {
        List<CategoryResponse.CategoryItem> tags = postTagService.getTagNamesByPostId(post.getId()).stream()
                .map(tagName -> CategoryResponse.CategoryItem.of(null, tagName, null))
                .collect(Collectors.toList());
        
        List<CategoryResponse.CategoryItem> categories = postCategoryService.getCategoryItemsByPostId(post.getId()).stream()
                .map(ci -> CategoryResponse.CategoryItem.of(ci.getId(), ci.getName(), null))
                .collect(Collectors.toList());

        return PostMeResponse.MyPostItem.of(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getExcerpt(),
                post.getStatus(),
                fileStorageService.getFileUrl(post.getThumbnailPath()),
                categories,
                tags,
                post.getCreatedAt()
        );
    }
}
