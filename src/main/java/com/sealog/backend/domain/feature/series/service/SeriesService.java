package com.sealog.backend.domain.feature.series.service;

import com.sealog.backend.domain.feature.series.dto.SeriesMeResponse;
import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
import com.sealog.backend.domain.feature.series.dto.SeriesResponse;
import com.sealog.backend.domain.feature.series.entity.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 시리즈(아카이브) 서비스 인터페이스
 */
public interface SeriesService {

    // ========== Guest (공개) ========== //

    /**
     * 특정 사용자(nickname)의 특정 시리즈에 속한 공개 게시글 목록 조회
     * - PUBLISHED 상태의 게시글만 반환
     *
     * @param nickname 시리즈 소유자 닉네임
     * @param slug     시리즈 slug
     * @param pageable 페이징 정보
     * @return 공개 게시글 목록
     */
    Page<SeriesResponse.PostItem> getPagedPostItemsByNickname(String nickname, String slug, Pageable pageable);

    /**
     * 특정 사용자(nickname)의 공개 시리즈 목록 페이징 조회
     * - isPublic이 true인 시리즈만 반환
     *
     * @param nickname 소유자 닉네임
     * @param pageable 페이징 정보
     * @return 공개 시리즈 목록
     */
    Page<SeriesResponse.SeriesItem> getPagedPublicItems(String nickname, Pageable pageable);

    // ========== User (인증/소유자) ========== //

    /**
     * 내 특정 시리즈에 속한 모든 게시글 목록 조회
     * - 공개/비공개 및 게시글 상태(DRAFT 등)와 무관하게 모든 게시글 반환
     *
     * @param userId   소유자(나)의 ID
     * @param slug     시리즈 slug
     * @param pageable 페이징 정보
     * @return 시리즈 내 전체 게시글 목록
     */
    Page<SeriesMeResponse.MyPostItem> getPagedPostItemsMe(Long userId, String slug, Pageable pageable);

    /**
     * 내 전체 시리즈 목록 페이징 조회
     * - 공개/비공개 여부와 무관하게 모두 반환
     *
     * @param userId   내 사용자 ID
     * @param pageable 페이징 정보
     * @return 내 전체 시리즈 목록
     */
    Page<SeriesMeResponse.MySeriesItem> getPagedItems(Long userId, Pageable pageable);

    /**
     * 시리즈 생성
     */
    void create(Long userId, SeriesMeRequest.Create request);

    /**
     * 시리즈 정보 수정
     */
    void update(Long userId, Long seriesId, SeriesMeRequest.Update request);

    /**
     * 시리즈 공개 처리
     */
    void show(Long userId, Long seriesId);

    /**
     * 시리즈 비공개 처리
     */
    void hide(Long userId, Long seriesId);

    /**
     * 시리즈 삭제
     */
    void delete(Long userId, Long seriesId);

    /**
     * 시리즈 안전 조회 (존재 및 소유권 검증)
     */
    Series getByIdAndUserId(Long seriesId, Long userId);
}
