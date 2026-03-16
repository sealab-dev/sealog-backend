package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.base.validation.annotation.CheckFile;
import com.sealog.backend.domain.base.validation.enums.AllowedFileType;
import com.sealog.backend.domain.feature.post.dto.PostMeResponse;
import com.sealog.backend.domain.feature.post.dto.PostMeRequest;
import com.sealog.backend.domain.feature.post.service.PostService;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Post (Me)", description = "내 게시글 관리 API (인증 필요)")
@SecurityRequirement(name = "Bearer Authentication")
@Validated
@RestController
@RequestMapping("/api/me/posts")
@RequiredArgsConstructor
public class PostMeController {

    private final PostService postService;

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다. (multipart/form-data)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공"),
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostMeResponse.MyPostItem create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") @Valid PostMeRequest.Create request,
            @CheckFile(allowed = {AllowedFileType.IMAGE}, maxSizeMB = 10, nullable = true)
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        return postService.create(userDetails.getUser(), request, thumbnail);
    }

    @Operation(summary = "게시글 수정용 데이터 조회", description = "수정 페이지에 필요한 기존 게시글 데이터를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/edit/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PostMeResponse.MyPostEdit getPostForEdit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시글 slug", example = "spring-boot-jpa") @PathVariable String slug
    ) {
        return postService.getEdit(userDetails.getUserId(), slug);
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다. (multipart/form-data)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
    })
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostMeResponse.MyPostItem update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "수정할 게시글 ID", example = "1") @PathVariable Long postId,
            @RequestPart("request") @Valid PostMeRequest.Update request,
            @CheckFile(allowed = {AllowedFileType.IMAGE}, maxSizeMB = 10, nullable = true)
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        return postService.update(userDetails.getUserId(), postId, request, thumbnail);
    }

    @Operation(summary = "게시글 삭제 (소프트 삭제)")
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.delete(userDetails.getUserId(), postId);
        return CustomResponse.success("게시글이 삭제되었습니다");
    }

    @Operation(summary = "게시글 복구", description = "삭제된 게시글을 복구합니다.")
    @PatchMapping("/{postId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> restore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.restore(userDetails.getUserId(), postId);
        return CustomResponse.success("게시글이 복구되었습니다");
    }

    @Operation(summary = "내 게시글 검색", description = "내 게시글 중에서 keyword로 검색합니다. (DRAFT 포함)")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostMeResponse.MyPostItem> searchPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "검색 키워드", example = "spring")
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostMeResponse.MyPostItem> results = postService.searchMyPosts(userDetails.getUser().getNickname(), keyword, pageable);
        return PageResponse.from(results);
    }

    @Operation(summary = "삭제된 게시글 목록 조회")
    @GetMapping("/deleted")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostMeResponse.MyPostItem> getDeleted(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostMeResponse.MyPostItem> results = postService.getDeleted(userDetails.getUserId(), pageable);
        return PageResponse.from(results);
    }
}
