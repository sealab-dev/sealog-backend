package com.sealog.backend.domain.feature.archive.controller;

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
 * Archive API 문서 인터페이스 (Swagger 전용, 인증 필수)
 *
 * 역할:
 * - 아카이브(모음집) 목록·게시글 조회
 * - 아카이브 CRUD
 * - 아카이브 공개/비공개
 * - 게시글-아카이브 배정/해제
 */
@Tag(name = "Archive", description = "아카이브(모음집) 관리 API (인증 필수)")
@SecurityRequirement(name = "bearerAuth")
public interface ArchiveUserControllerDocs {

    @Operation(
            summary = "내 아카이브 목록 조회",
            description = "로그인 사용자의 전체 아카이브 목록을 페이징 조회합니다. (공개/비공개 포함)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.ArchiveItems>>> getPagedItemsForUser(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            Pageable pageable
    );

    @Operation(
            summary = "내 아카이브 게시글 목록 조회",
            description = "로그인 사용자의 아카이브에 속한 게시글 목록을 페이징 조회합니다. (PUBLISHED/DRAFT 포함)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<PageResponse<ArchiveResponse.PostItems>>> getPagedPostItemsByUserIdAndArchiveIdForUser(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "아카이브 ID", example = "1") Long archiveId,
            Pageable pageable
    );

    @Operation(
            summary = "아카이브 생성",
            description = "로그인 사용자의 아카이브(모음집)를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> add(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            ArchiveRequest.Add request
    );

    @Operation(
            summary = "아카이브 수정",
            description = "아카이브 이름(name)을 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> edit(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "소유자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "아카이브 slug", example = "spring-boot-study") String slug,
            ArchiveRequest.Edit request
    );

    @Operation(
            summary = "아카이브 공개",
            description = "아카이브를 공개 상태로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공개 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> show(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "소유자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "아카이브 slug", example = "spring-boot-study") String slug
    );

    @Operation(
            summary = "아카이브 비공개",
            description = "아카이브를 비공개 상태로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비공개 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> hide(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "소유자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "아카이브 slug", example = "spring-boot-study") String slug
    );

    @Operation(
            summary = "아카이브 삭제",
            description = "아카이브를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> remove(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "소유자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "아카이브 slug", example = "spring-boot-study") String slug
    );

    @Operation(
            summary = "게시글 아카이브 배정",
            description = "게시글을 특정 아카이브에 배정합니다. 이미 다른 아카이브에 속해 있는 경우 교체됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (게시글 또는 아카이브 소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> changePostArchive(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "배정할 아카이브 ID", example = "1") Long archiveId,
            @Parameter(description = "배정할 게시글 ID", example = "1") Long postId
    );

    @Operation(
            summary = "게시글 아카이브 해제",
            description = "게시글의 아카이브 배정을 해제합니다. 게시글의 archive 필드를 null로 설정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "해제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (게시글 소유자 아님)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<CustomResponse<Void>> removePostArchive(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "해제할 게시글 ID", example = "1") Long postId
    );
}