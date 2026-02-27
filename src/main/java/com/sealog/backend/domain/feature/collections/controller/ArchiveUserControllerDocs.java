package com.sealog.backend.domain.feature.collections.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Archive API 문서 인터페이스(Swagger 전용)
 *
 * - Post 조회/검색 같은 기능은 PostController에서 담당 (여기서 제거)
 * - 여기서는 "아카이브(모음집) 자체" CRUD 제공
 *
 * 도메인 구조
 * - Archive: 사용자(User)가 소유하는 모음집 엔티티
 *   - name: 모음집 제목
 *   - slug: URL-safe 식별자 (UNIQUE)
 *   - isPublic: 공개/비공개
 *
 */
@Tag(name = "Archive", description = "아카이브(모음집) 관리 API")
@SecurityRequirements()
public interface ArchiveUserControllerDocs {

    /**
     * 아카이브 생성
     */
    @Operation(
            summary = "아카이브 생성",
            description = "로그인 사용자의 아카이브(모음집)를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "slug 중복 등 리소스 충돌"
            )
    })
    void createArchive(
            
    );

    /**
     * 아카이브 조회
     **/
    @Operation(
            summary = "내 아카이브 목록 조회",
            description = "내 아카이브 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음"
            )
    })
    void getArchivesList(

    );

    /**
     * 아카이브 수정
     */
    @Operation(
            summary = "아카이브 수정",
            description = "아카이브의 타이틀(name) 변경 및 공개 여부(isPublic)를 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "아카이브 없음"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음(소유자 아님)"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "slug 중복 등 리소스 충돌"
            )
    })
    void updateArchive(
            @Parameter(description = "아카이브 slug (URL-safe 식별자)", example = "spring-boot-study")
            String archiveSlug
    );

    /**
     * 아카이브 삭제
     */
    @Operation(
            summary = "아카이브 삭제",
            description = "아카이브를 삭제합니다. 삭제 시 연결된 ArchiveItem 정리 정책을 함께 적용합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "아카이브 없음"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음(소유자 아님)"
            )
    })
    void deleteArchive(
            @Parameter(description = "아카이브 slug", example = "spring-boot-study")
            String archiveSlug
    );

}