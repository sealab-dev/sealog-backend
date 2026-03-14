package com.sealog.backend.domain.feature.series.service;

import com.sealog.backend.domain.base.util.SlugUtils;
import com.sealog.backend.domain.feature.series.dto.SeriesRequest;
import com.sealog.backend.domain.feature.series.dto.SeriesResponse;
import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.domain.feature.series.repository.SeriesRepository;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * SeriesService 구현 클래스
 */
@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class SeriesServiceImpl implements SeriesService {

    // 사용 의존성
    private final SeriesRepository seriesRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public Page<SeriesResponse.PostItems> getPagedPostItems(Long requesterId, String nickname, String slug, Pageable pageable) {

        // 1. 아카이브 조회
        Series series = findSeriesByNicknameAndSlug(nickname, slug);

        // 2. User: 소유자 검증 후 전체 상태 반환
        if (Objects.nonNull(requesterId)) {
            verifyOwner(series, requesterId);
            return postRepository
                    .findByUserIdAndSeriesId(requesterId, series.getId(), pageable)
                    .map(this::toPostItems);
        }

        // 3. Guest: PUBLISHED만 반환
        return postRepository
                .findBySeriesIdAndStatus(series.getId(), PostStatus.PUBLISHED, pageable)
                .map(this::toPostItems);
    }

    @Override
    public Page<SeriesResponse.SeriesItems> getPagedPublicItems(String nickname, Pageable pageable) {

        return seriesRepository
                .findByUserNicknameAndIsPublic(nickname, true, pageable)
                .map(this::toItems);
    }

    @Override
    public Page<SeriesResponse.SeriesItems> getPagedItems(Long userId, Pageable pageable) {

        return seriesRepository
                .findByUserId(userId, pageable)
                .map(this::toItems);
    }

    @Transactional
    @Override
    public void create(Long userId, SeriesRequest.Create request) {

        // 1. 회원 엔티티 조회
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> CustomException.notFound("존재하지 않거나 탈퇴한 사용자의 아카이브를 생성할 수 없습니다."));

        // 2. 검증
        String name = request.getName().trim();
        String slug = SlugUtils.generate(name);

        // 같은 이름 생성 시도 차단
        if (seriesRepository.existsByNameAndUserId(name, userId))
            throw CustomException.badRequest("이미 같은 이름의 아카이브가 존재합니다.");

        // 3. entity 생성
        Series series = Series.builder()
                .user(user)
                .name(name)
                .slug(slug)
                .isPublic(true)
                .build();

        // 4. 저장
        seriesRepository.save(series);
    }

    @Transactional
    @Override
    public void update(Long userId, Long seriesId, SeriesRequest.Update request) {

        // 1. entity 조회 및 검증
        Series series = getByIdAndUserId(seriesId, userId);

        // 2. 이름 중복 검사
        String editName = request.getName().trim();

        // 동일 이름으로 수정 차단
        if (Objects.equals(series.getName(), editName))
            throw CustomException.badRequest("같은 이름으로 변경할 수 없습니다.");

        // 다른 아카이브와 이름 중복 차단
        if (seriesRepository.existsByNameAndUserId(editName, userId))
            throw CustomException.conflict("이미 같은 이름의 아카이브가 존재합니다.");

        // 3. 연관관계 메소드 기반 갱신
        series.edit(editName, SlugUtils.generate(editName));
    }

    @Transactional
    @Override
    public void show(Long userId, Long seriesId) {

        // 1. 조회 및 검증
        Series series = getByIdAndUserId(seriesId, userId);

        // 2. 공개 상태로 변경
        series.editIsPublic(true);
    }


    @Transactional
    @Override
    public void hide(Long userId, Long seriesId) {

        // 1. 조회 및 검증
        Series series = getByIdAndUserId(seriesId, userId);

        // 2. 비공개 상태로 변경
        series.editIsPublic(false);
    }


    @Transactional
    @Override
    public void delete(Long userId, Long seriesId) {

        // 1. 조회 및 검증
        Series series = getByIdAndUserId(seriesId, userId);

        // 2. 삭제 수행
        seriesRepository.delete(series);
    }


    @Override
    public Series getByIdAndUserId(Long seriesId, Long userId) {

        // 1. 엔티티 조회
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> CustomException.notFound("아카이브를 찾을 수 없습니다."));

        // 2. 소유권 검증
        verifyOwner(series, userId);

        return series;
    }


    /**
     * Entity -> DTO 변환 메소드
     */
    private SeriesResponse.SeriesItems toItems(Series entity) {
        return SeriesResponse.SeriesItems.of(entity.getId(), entity.getSlug(), entity.getName());
    }

    private SeriesResponse.PostItems toPostItems(Post entity) {
        return SeriesResponse.PostItems.of(entity.getId(), entity.getTitle(), entity.getSlug(), entity.getThumbnailPath());
    }


    /**
     * 엔티티 조회 메소드
     */
    private Series findSeriesByNicknameAndSlug(String nickname, String slug) {

        return seriesRepository
                .findByNicknameAndSlug(nickname, slug)
                .orElseThrow(() -> CustomException.notFound("존재하지 않거나 이미 삭제된 아카이브입니다."));
    }


    /**
     * 검증 메소드
     */
    private void verifyOwner(Series series, Long requestUserId) {

        if (!series.isOwnedBy(requestUserId))
            throw CustomException.forbidden("다른 사용자의 아카이브를 변경할 수 없습니다.");
    }

}