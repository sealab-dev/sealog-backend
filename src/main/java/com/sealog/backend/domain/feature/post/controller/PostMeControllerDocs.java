package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.base.validation.annotation.CheckFile;
import com.sealog.backend.domain.base.validation.enums.AllowedFileType;
import com.sealog.backend.domain.feature.post.dto.PostMeResponse;
import com.sealog.backend.domain.feature.post.dto.PostMeRequest;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Post (Me)", description = "내 게시글 관리 API (인증 필요)")
@SecurityRequirement(name = "Bearer Authentication")
public interface PostMeControllerDocs {

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다. (multipart/form-data)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
    })
    PostMeResponse.MyPostItem create(
            CustomUserDetails userDetails,
            @Valid PostMeRequest.Create request,
            @CheckFile(allowed = {AllowedFileType.IMAGE}, maxSizeMB = 10, nullable = true)
            MultipartFile thumbnail
    );

    @Operation(summary = "게시글 수정용 데이터 조회", description = "수정 페이지에 필요한 기존 게시글 데이터를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    PostMeResponse.MyPostEdit getPostForEdit(
            CustomUserDetails userDetails,
            @Parameter(description = "게시글 slug", example = "spring-boot-jpa") String slug
    );

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다. (multipart/form-data)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
    })
    PostMeResponse.MyPostItem update(
            CustomUserDetails userDetails,
            @Parameter(description = "수정할 게시글 ID", example = "1") Long postId,
            @Valid PostMeRequest.Update request,
            @CheckFile(allowed = {AllowedFileType.IMAGE}, maxSizeMB = 10, nullable = true)
            MultipartFile thumbnail
    );

    @Operation(summary = "게시글 삭제 (소프트 삭제)")
    CustomResponse<Void> delete(CustomUserDetails userDetails, Long postId);

    @Operation(summary = "게시글 복구", description = "삭제된 게시글을 복구합니다.")
    CustomResponse<Void> restore(CustomUserDetails userDetails, Long postId);

    @Operation(summary = "내 게시글 검색", description = "내 게시글 중에서 keyword로 검색합니다. (DRAFT 포함)")
    PageResponse<PostMeResponse.MyPostItem> searchPosts(
            CustomUserDetails userDetails,
            @Parameter(description = "검색 키워드", example = "spring") String keyword,
            Pageable pageable
    );

    @Operation(summary = "삭제된 게시글 목록 조회")
    PageResponse<PostMeResponse.MyPostItem> getDeleted(
            CustomUserDetails userDetails,
            Pageable pageable
    );

}
