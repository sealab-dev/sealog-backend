package com.sealog.backend.domain.feature.series.service;

import com.sealog.backend.domain.feature.series.dto.SeriesMeResponse;
import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
import com.sealog.backend.domain.feature.series.dto.SeriesResponse;
import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.global.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 시리즈(아카이브) 도메인 비즈니스 규약 인터페이스
 */
public interface SeriesService {

    // ========== Guest (공개) ========== //

    /**
     * 특정 사용자(nickname)의 특정 시리즈에 속한 공개 게시글 목록을 조회합니다.
     * - PUBLISHED 상태의 게시글만 반환됩니다.
     *
     * @param nickname 시리즈 소유자 닉네임
     * @param slug     시리즈 slug
     * @param pageable 페이징 정보
     * @return 공개 게시글 목록 페이지
     * @throws CustomException.notFound 시리즈를 찾을 수 없는 경우 발생
     */
    Page<SeriesResponse.SeriesPostItem> getPagedPostItemsByNickname(String nickname, String slug, Pageable pageable);

    /**
     * 특정 사용자(nickname)의 공개 시리즈 목록을 페이징 조회합니다.
     * - isPublic이 true인 시리즈만 반환됩니다.
     *
     * @param nickname 소유자 닉네임
     * @param pageable 페이징 정보
     * @return 공개 시리즈 목록 페이지
     */
    Page<SeriesResponse.SeriesItem> getPagedPublicItems(String nickname, Pageable pageable);

    // ========== User (인증/소유자) ========== //

    /**
     * 내 특정 시리즈에 속한 모든 게시글 목록을 조회합니다.
     * - 공개/비공개 및 모든 게시글 상태(PUBLISHED, DRAFT 등)를 포함합니다.
     *
     * @param userId   소유자(나)의 ID
     * @param nickname 소유자(나)의 닉네임
     * @param slug     시리즈 slug
     * @param pageable 페이징 정보
     * @return 시리즈 내 전체 게시글 목록 페이지
     * @throws CustomException.notFound  시리즈를 찾을 수 없는 경우 발생
     * @throws CustomException.forbidden 본인의 시리즈가 아닐 경우 발생
     */
    Page<SeriesMeResponse.MySeriesPostItem> getPagedPostItemsMe(Long userId, String nickname, String slug, Pageable pageable);

    /**
     * 내 전체 시리즈 목록을 페이징 조회합니다.
     * - 공개/비공개 여부와 무관하게 모든 시리즈가 반환됩니다.
     *
     * @param userId   내 사용자 ID
     * @param pageable 페이징 정보
     * @return 내 전체 시리즈 목록 페이지
     */
    Page<SeriesMeResponse.MySeriesItem> getPagedItems(Long userId, Pageable pageable);

    /**
     * 새로운 시리즈를 생성합니다.
     *
     * @param userId  작성자 ID
     * @param request 생성 요청 데이터
     * @throws CustomException.notFound  사용자를 찾을 수 없는 경우 발생
     * @throws CustomException.badRequest 이미 동일한 이름의 시리즈가 존재할 경우 발생
     */
    void create(Long userId, SeriesMeRequest.Create request);

    /**
     * 시리즈 정보를 수정합니다.
     *
     * @param userId   수정 요청자 ID
     * @param seriesId 수정할 시리즈 ID
     * @param request  수정 요청 데이터
     * @throws CustomException.notFound  시리즈를 찾을 수 없는 경우 발생
     * @throws CustomException.forbidden 본인의 시리즈가 아닐 경우 발생
     * @throws CustomException.badRequest 기존 이름과 동일하거나 이미 존재하는 이름일 경우 발생
     */
    void update(Long userId, Long seriesId, SeriesMeRequest.Update request);

    /**
     * 시리즈를 공개로 전환합니다.
     *
     * @param userId   요청자 ID
     * @param seriesId 공개할 시리즈 ID
     */
    void show(Long userId, Long seriesId);

    /**
     * 시리즈를 비공개로 전환합니다.
     *
     * @param userId   요청자 ID
     * @param seriesId 비공개할 시리즈 ID
     */
    void hide(Long userId, Long seriesId);

    /**
     * 시리즈를 삭제합니다.
     *
     * @param userId   삭제 요청자 ID
     * @param seriesId 삭제할 시리즈 ID
     * @throws CustomException.notFound 시리즈를 찾을 수 없거나 이미 삭제된 경우 발생
     */
    void delete(Long userId, Long seriesId);

    /**
     * 시리즈 정보를 안전하게 조회합니다. (존재 여부 및 소유권 검증 포함)
     *
     * @param seriesId 조회할 시리즈 ID
     * @param userId   검증할 사용자 ID
     * @return 검증된 시리즈 엔티티
     * @throws CustomException.notFound  시리즈가 존재하지 않을 경우 발생
     * @throws CustomException.forbidden 소유자가 일치하지 않을 경우 발생
     */
    Series getByIdAndUserId(Long seriesId, Long userId);
}
