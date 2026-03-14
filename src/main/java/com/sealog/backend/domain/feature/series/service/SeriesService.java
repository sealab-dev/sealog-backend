package com.sealog.backend.domain.feature.series.service;

import com.sealog.backend.domain.feature.series.dto.SeriesRequest;
import com.sealog.backend.domain.feature.series.dto.SeriesResponse;
import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.global.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 아카이브 서비스 인터페이스
 */
public interface SeriesService {

    // ========== Guest (공개) ========== //

    /**
     * 아카이브에 속한 게시글 목록 페이징 조회
     * - requesterId null → PUBLISHED 상태만 반환 (Guest)
     * - requesterId non-null → 소유자 검증 후 전체 상태 반환 (User)
     *
     * @param requesterId 요청 사용자 ID (null = 비인증 Guest)
     * @param nickname    아카이브 소유자 닉네임
     * @param slug        아카이브 slug
     * @param pageable    페이징 정보
     * @return 게시글 목록
     * @throws CustomException 아카이브 없음, 권한 없음 (User 요청 시)
     */
    /**
     * 아카이브에 속한 게시글 목록 페이징 조회
     * - requesterId null → PUBLISHED 상태만 반환 (Guest)
     * - requesterId non-null → 소유자 검증 후 전체 상태 반환 (User)
     *
     * @param requesterId 요청 사용자 ID (null = 비인증 Guest)
     * @param nickname    아카이브 소유자 닉네임
     * @param slug        아카이브 slug
     * @param pageable    페이징 정보
     * @return 게시글 목록
     * @throws CustomException 아카이브 없음, 권한 없음 (User 요청 시)
     */
    Page<SeriesResponse.PostItems> getPagedPostItems(Long requesterId, String nickname, String slug, Pageable pageable);

    /**
     * 특정 사용자(nickname)의 공개 아카이브 목록 페이징 조회 (Guest용)
     *
     * @param nickname 소유자 닉네임
     * @param pageable 페이징 정보
     * @return 공개 아카이브 목록
     */
    Page<SeriesResponse.SeriesItems> getPagedPublicItems(String nickname, Pageable pageable);

    // ========== User (인증) ========== //

    /**
     * 사용자 본인의 전체 아카이브 목록 페이징 조회
     * - 공개/비공개 여부 무관하게 모두 반환
     *
     * @param userId   조회 대상 사용자 ID
     * @param pageable 페이징 정보
     * @return 전체 아카이브 목록 (공개·비공개 포함)
     */
    Page<SeriesResponse.SeriesItems> getPagedItems(Long userId, Pageable pageable);

    /**
     * 아카이브 생성
     * - 동일 사용자 기준 이름 중복 불가
     *
     * @param userId  생성 요청 사용자 ID
     * @param request 생성 요청 데이터
     * @throws CustomException 사용자 없음, 동일 이름 이미 존재
     */
    void create(Long userId, SeriesRequest.Create request);

    /**
     * 아카이브 이름 수정
     *
     * @param userId    수정 요청 사용자 ID
     * @param seriesId 수정할 아카이브 ID
     * @param request   수정 요청 데이터
     * @throws CustomException 아카이브 없음, 권한 없음, 동일 이름으로 수정 시도
     */
    void update(Long userId, Long seriesId, SeriesRequest.Update request);

    /**
     * 아카이브 공개 처리
     *
     * @param userId    요청 사용자 ID
     * @param seriesId 대상 아카이브 ID
     * @throws CustomException 아카이브 없음, 권한 없음
     */
    void show(Long userId, Long seriesId);

    /**
     * 아카이브 비공개 처리
     *
     * @param userId    요청 사용자 ID
     * @param seriesId 대상 아카이브 ID
     * @throws CustomException 아카이브 없음, 권한 없음
     */
    void hide(Long userId, Long seriesId);

    /**
     * 아카이브 삭제
     *
     * @param userId    삭제 요청 사용자 ID
     * @param seriesId 삭제할 아카이브 ID
     * @throws CustomException 아카이브 없음, 권한 없음
     */
    void delete(Long userId, Long seriesId);

    /**
     * 특정 사용자의 아카이브를 안전하게 조회 (존재 여부 및 소유권 검증)
     *
     * @param seriesId 아카이브 ID
     * @param userId    사용자 ID
     * @return 검증된 Series 엔티티
     * @throws CustomException 아카이브 없음, 권한 없음
     */
    Series getByIdAndUserId(Long seriesId, Long userId);
}