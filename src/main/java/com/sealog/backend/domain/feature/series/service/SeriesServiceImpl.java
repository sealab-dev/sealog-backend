package com.sealog.backend.domain.feature.series.service;

import com.sealog.backend.domain.base.util.SlugUtils;
import com.sealog.backend.domain.feature.series.dto.SeriesMeResponse;
import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
import com.sealog.backend.domain.feature.series.dto.SeriesResponse;
import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.domain.feature.series.repository.SeriesRepository;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.domain.feature.post.service.PostCategoryService;
import com.sealog.backend.domain.feature.post.service.PostTagService;
import com.sealog.backend.infra.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class SeriesServiceImpl implements SeriesService {

    private final SeriesRepository seriesRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostTagService postTagService;
    private final PostCategoryService postCategoryService;
    private final FileStorageService fileStorageService;

    // ========== Guest (공개) ========== //

    @Override
    public Page<SeriesResponse.SeriesPostItem> getPagedPostItemsByNickname(String nickname, String slug, Pageable pageable) {
        Series series = seriesRepository.findByNicknameAndSlug(nickname, slug)
                .orElseThrow(() -> CustomException.notFound("시리즈를 찾을 수 없습니다."));

        return postRepository.findBySeriesIdAndStatus(series.getId(), PostStatus.PUBLISHED, pageable)
                .map(this::toPublicSeriesPostItem);
    }

    @Override
    public Page<SeriesResponse.SeriesItem> getPagedPublicItems(String nickname, Pageable pageable) {
        return seriesRepository.findByUserNicknameAndIsPublic(nickname, true, pageable)
                .map(this::toPublicSeriesItem);
    }

    // ========== User (인증/소유자) ========== //

    @Override
    public Page<SeriesMeResponse.MySeriesPostItem> getPagedPostItemsMe(Long userId, String nickname, String slug, Pageable pageable) {
        Series series = seriesRepository.findByNicknameAndSlug(nickname, slug)
                .orElseThrow(() -> CustomException.notFound("시리즈를 찾을 수 없습니다."));
        
        verifyOwner(series, userId);

        return postRepository.findByUserIdAndSeriesId(userId, series.getId(), pageable)
                .map(this::toMeSeriesPostItem);
    }

    @Override
    public Page<SeriesMeResponse.MySeriesItem> getPagedItems(Long userId, Pageable pageable) {
        return seriesRepository.findByUserId(userId, pageable)
                .map(this::toMeSeriesItem);
    }

    @Transactional
    @Override
    public void create(Long userId, SeriesMeRequest.Create request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다."));

        String name = request.getName().trim();
        if (seriesRepository.existsByNameAndUserId(name, userId)) {
            throw CustomException.badRequest("이미 존재하는 시리즈 이름입니다.");
        }

        Series series = Series.builder()
                .user(user)
                .name(name)
                .slug(SlugUtils.generate(name))
                .isPublic(true)
                .build();

        seriesRepository.save(series);
    }

    @Transactional
    @Override
    public void update(Long userId, Long seriesId, SeriesMeRequest.Update request) {
        Series series = getByIdAndUserId(seriesId, userId);
        String editName = request.getName().trim();

        if (Objects.equals(series.getName(), editName)) {
            throw CustomException.badRequest("동일한 이름으로 변경할 수 없습니다.");
        }

        if (seriesRepository.existsByNameAndUserId(editName, userId)) {
            throw CustomException.conflict("이미 존재하는 시리즈 이름입니다.");
        }

        series.edit(editName, SlugUtils.generate(editName));
    }

    @Transactional
    @Override
    public void show(Long userId, Long seriesId) {
        getByIdAndUserId(seriesId, userId).editIsPublic(true);
    }

    @Transactional
    @Override
    public void hide(Long userId, Long seriesId) {
        getByIdAndUserId(seriesId, userId).editIsPublic(false);
    }

    @Transactional
    @Override
    public void delete(Long userId, Long seriesId) {
        Series series = getByIdAndUserId(seriesId, userId);
        seriesRepository.delete(series);
    }

    @Override
    public Series getByIdAndUserId(Long seriesId, Long userId) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> CustomException.notFound("시리즈를 찾을 수 없습니다."));
        verifyOwner(series, userId);
        return series;
    }

    // ========== Private 보조 메소드 (DTO 변환 및 검증) ========== //

    /**
     * 공개용 시리즈 항목 DTO를 생성합니다.
     */
    private SeriesResponse.SeriesItem toPublicSeriesItem(Series entity) {
        long postCount = postRepository.countBySeriesIdAndStatusAndDeletedAtIsNull(entity.getId(), PostStatus.PUBLISHED);
        return SeriesResponse.SeriesItem.of(entity.getId(), entity.getSlug(), entity.getName(), postCount);
    }

    /**
     * 시리즈 내 게시글 항목 DTO를 생성합니다. (공개용)
     */
    private SeriesResponse.SeriesPostItem toPublicSeriesPostItem(Post entity) {
        List<String> tags = postTagService.getTagNamesByPostId(entity.getId());
        List<SeriesResponse.CategoryItem> categories = postCategoryService.getPostCategoriesByPostId(entity.getId()).stream()
                .map(pc -> SeriesResponse.CategoryItem.of(pc.getCategory().getId(), pc.getCategory().getName(), pc.getSortOrder()))
                .collect(Collectors.toList());

        SeriesResponse.AuthorInfo author = SeriesResponse.AuthorInfo.of(
                entity.getUser().getNickname(),
                fileStorageService.getFileUrl(entity.getUser().getProfileImagePath())
        );

        return SeriesResponse.SeriesPostItem.of(
                entity.getId(),
                entity.getSlug(),
                entity.getTitle(),
                entity.getExcerpt(),
                entity.getStatus(),
                fileStorageService.getFileUrl(entity.getThumbnailPath()),
                tags,
                categories,
                author,
                entity.getCreatedAt()
        );
    }

    /**
     * 내 시리즈 목록 항목 DTO를 생성합니다.
     */
    private SeriesMeResponse.MySeriesItem toMeSeriesItem(Series entity) {
        long postCount = postRepository.countBySeriesIdAndDeletedAtIsNull(entity.getId());
        return SeriesMeResponse.MySeriesItem.of(entity.getId(), entity.getSlug(), entity.getName(), entity.isPublic(), postCount);
    }

    /**
     * 내 시리즈 내 게시글 항목 DTO를 생성합니다. (관리용)
     */
    private SeriesMeResponse.MySeriesPostItem toMeSeriesPostItem(Post entity) {
        List<String> tags = postTagService.getTagNamesByPostId(entity.getId());
        List<SeriesMeResponse.MyCategoryItem> categories = postCategoryService.getPostCategoriesByPostId(entity.getId()).stream()
                .map(pc -> SeriesMeResponse.MyCategoryItem.of(pc.getCategory().getId(), pc.getCategory().getName(), pc.getSortOrder()))
                .collect(Collectors.toList());

        return SeriesMeResponse.MySeriesPostItem.of(
                entity.getId(),
                entity.getSlug(),
                entity.getTitle(),
                entity.getExcerpt(),
                entity.getStatus(),
                fileStorageService.getFileUrl(entity.getThumbnailPath()),
                tags,
                categories,
                entity.getCreatedAt()
        );
    }

    /**
     * 시리즈 소유자 권한을 검증합니다.
     */
    private void verifyOwner(Series series, Long userId) {
        if (!series.getUser().getId().equals(userId)) {
            throw CustomException.forbidden("권한이 없습니다.");
        }
    }
}
