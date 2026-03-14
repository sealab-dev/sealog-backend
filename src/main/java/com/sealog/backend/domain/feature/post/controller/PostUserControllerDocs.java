package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.feature.post.dto.PostRequest;
import com.sealog.backend.domain.feature.post.dto.PostResponse;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Post", description = "내 게시글 API (인증 필수)")
@SecurityRequirement(name = "bearerAuth")
public interface PostUserControllerDocs {

    @Operation(summary = "게시글 생성", description = "multipart/form-data로 게시글을 생성합니다. request(JSON)와 thumbnail(이미지, 선택)을 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<PostResponse.Detail>> create(
            CustomUserDetails userDetails,
            PostRequest.Create request,
            @Parameter(description = "썸네일 이미지 파일 (선택)") MultipartFile thumbnail
    );

    @Operation(summary = "게시글 수정용 데이터 조회", description = "에디터에서 수정할 수 있도록 게시글 데이터를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<PostResponse.Edit>> getPostForEdit(
            CustomUserDetails userDetails,
            @Parameter(description = "게시글 slug", example = "spring-boot-jpa") String slug
    );

    @Operation(summary = "게시글 수정", description = "multipart/form-data로 게시글을 수정합니다. thumbnail이 없으면 기존 썸네일을 유지합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<PostResponse.Detail>> update(
            CustomUserDetails userDetails,
            @Parameter(description = "게시글 ID", example = "1") Long postId,
            PostRequest.Update request,
            @Parameter(description = "새 썸네일 이미지 파일 (선택, 없으면 기존 유지)") MultipartFile thumbnail
    );

    @Operation(summary = "게시글 삭제", description = "postId 기준으로 내 게시글을 소프트 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<Void> delete(
            CustomUserDetails userDetails,
            @Parameter(description = "게시글 ID", example = "1") Long postId
    );

    @Operation(summary = "게시글 복구", description = "소프트 삭제된 내 게시글을 복구합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "복구 성공"),
            @ApiResponse(responseCode = "400", description = "삭제되지 않은 게시글",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<Void>> restore(
            CustomUserDetails userDetails,
            @Parameter(description = "게시글 ID", example = "1") Long postId
    );

    @Operation(summary = "내 게시글 검색", description = "keyword로 본인의 게시글(PUBLISHED + DRAFT)을 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> searchPosts(
            CustomUserDetails userDetails,
            @Parameter(description = "검색 키워드", example = "spring") String keyword,
            Pageable pageable
    );

    @Operation(summary = "삭제된 내 게시글 목록 조회", description = "소프트 삭제된 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> getDeleted(
            CustomUserDetails userDetails,
            Pageable pageable
    );
}
