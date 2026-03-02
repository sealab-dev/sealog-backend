package com.sealog.backend.domain.feature.archive.controller;

import com.sealog.backend.domain.feature.archive.dto.ArchivePostRequest;
import com.sealog.backend.domain.feature.archive.dto.ArchivePostResponse;
import com.sealog.backend.domain.feature.archive.dto.ArchiveRequest;
import com.sealog.backend.domain.feature.archive.dto.ArchiveResponse;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * Archive API 문서 인터페이스(Swagger 전용, 인증 필수)
 *
 * - 아카이브(모음집) 자체 CRUD
 * - 게시글-아카이브 연결/해제
 */
@Tag(name = "Archive", description = "아카이브(모음집) 관리 API (인증 필수)")
@SecurityRequirement(name = "bearerAuth")
public interface ArchiveUserControllerDocs {

    @Operation(summary = "내 아카이브 목록 조회", description = "내 아카이브 목록을 페이징 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.ArchiveItems>>> getMyArchives(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            Pageable pageable
    );

    @Operation(summary = "아카이브 생성", description = "로그인 사용자의 아카이브(모음집)를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> createArchive(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            ArchiveRequest.Add request
    );

    @Operation(summary = "아카이브 수정", description = "아카이브 이름(name)을 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음(소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> updateArchive(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "소유자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "아카이브 slug", example = "spring-boot-study") String slug,
            ArchiveRequest.Edit request
    );

    @Operation(summary = "아카이브 공개", description = "아카이브를 공개 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공개 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음(소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> showArchive(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "소유자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "아카이브 slug", example = "spring-boot-study") String slug
    );

    @Operation(summary = "아카이브 비공개", description = "아카이브를 비공개 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비공개 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음(소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> hideArchive(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "소유자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "아카이브 slug", example = "spring-boot-study") String slug
    );

    @Operation(summary = "아카이브 삭제", description = "아카이브를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음(소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> deleteArchive(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "소유자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "아카이브 slug", example = "spring-boot-study") String slug
    );

    @Operation(summary = "게시글 아카이브 추가", description = "게시글을 아카이브(들)에 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "추가 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> addArchivePost(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            ArchivePostRequest.Add request
    );

    @Operation(summary = "게시글 아카이브 제거", description = "게시글-아카이브 연결을 해제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제거 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> removeArchivePost(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "게시글 아카이브 ID", example = "1") Long archivePostId
    );
}
