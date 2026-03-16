package com.sealog.backend.domain.feature.series.controller;

import com.sealog.backend.domain.feature.series.dto.SeriesMeResponse;
import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
import com.sealog.backend.domain.feature.series.service.SeriesService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Series (Me)", description = "내 시리즈 관리 API (로그인 필요) — 공개/비공개 시리즈 모두 포함합니다.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/me/series")
@RequiredArgsConstructor
public class SeriesMeController {

    private final SeriesService seriesService;

    @Operation(
            summary = "내 시리즈 목록 조회",
            description = "내가 생성한 모든 시리즈 목록(공개/비공개 포함)을 최신순으로 페이지 조회합니다. 응답 data: `PageResponse<MySeriesItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesMeResponse.MySeriesItem> getPagedItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesMeResponse.MySeriesItem> items = seriesService.getPagedItems(userDetails.getUserId(), pageable);
        return PageResponse.from(items);
    }

    @Operation(
            summary = "내 시리즈 게시글 목록 조회",
            description = "내 특정 시리즈에 속한 게시글 목록(전체 상태 포함)을 조회합니다. 응답 data: `PageResponse<MySeriesPostItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "시리즈 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SeriesMeResponse.MySeriesPostItem> getPagedPostItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "시리즈 슬러그", example = "spring-series") @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SeriesMeResponse.MySeriesPostItem> items = seriesService.getPagedPostItemsMe(userDetails.getUserId(), slug, pageable);
        return PageResponse.from(items);
    }

    @Operation(
            summary = "시리즈 생성",
            description = "새로운 시리즈를 생성합니다. 생성 시 비공개 상태로 시작됩니다. **Request Body**: `SeriesCreate` (name)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공 (message: '시리즈가 생성되었습니다')",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "요청값 오류 (이름 누락 또는 길이 초과)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomResponse<Void> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SeriesMeRequest.Create request
    ) {
        seriesService.create(userDetails.getUserId(), request);
        return CustomResponse.success("시리즈가 생성되었습니다");
    }

    @Operation(
            summary = "시리즈 수정",
            description = "시리즈 이름을 수정합니다. **Request Body**: `SeriesUpdate` (name)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공 (message: '시리즈 정보가 수정되었습니다')",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "요청값 오류",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "타인의 시리즈 수정 시도",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "시리즈 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PutMapping("/{seriesId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse<Void> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "수정할 시리즈 ID", example = "1") @PathVariable Long seriesId,
            @Valid @RequestBody SeriesMeRequest.Update request
    ) {
        seriesService.update(userDetails.getUserId(), seriesId, request);
        return CustomResponse.success("시리즈 정보가 수정되었습니다");
    }

    @Operation(
            summary = "시리즈 공개 처리",
            description = "시리즈를 공개 상태로 전환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공개 전환 성공 (message: '시리즈가 공개로 전환되었습니다')",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "타인의 시리즈 접근 시도",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "시리즈 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PatchMapping("/{seriesId}/show")
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse<Void> show(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "공개 처리할 시리즈 ID", example = "1") @PathVariable Long seriesId
    ) {
        seriesService.show(userDetails.getUserId(), seriesId);
        return CustomResponse.success("시리즈가 공개로 전환되었습니다");
    }

    @Operation(
            summary = "시리즈 비공개 처리",
            description = "시리즈를 비공개 상태로 전환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비공개 전환 성공 (message: '시리즈가 비공개로 전환되었습니다')",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "타인의 시리즈 접근 시도",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "시리즈 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PatchMapping("/{seriesId}/hide")
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse<Void> hide(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "비공개 처리할 시리즈 ID", example = "1") @PathVariable Long seriesId
    ) {
        seriesService.hide(userDetails.getUserId(), seriesId);
        return CustomResponse.success("시리즈가 비공개로 전환되었습니다");
    }

    @Operation(
            summary = "시리즈 삭제",
            description = "시리즈를 삭제합니다. 삭제 시 연결된 게시글과의 연관 관계도 함께 해제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공 (message: '시리즈가 삭제되었습니다')",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "타인의 시리즈 삭제 시도",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "시리즈 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @DeleteMapping("/{seriesId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "삭제할 시리즈 ID", example = "1") @PathVariable Long seriesId
    ) {
        seriesService.delete(userDetails.getUserId(), seriesId);
        return CustomResponse.success("시리즈가 삭제되었습니다");
    }
}
