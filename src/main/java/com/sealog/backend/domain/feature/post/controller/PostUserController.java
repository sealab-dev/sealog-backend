package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.feature.post.dto.PostRequest;
import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.service.PostService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 내 게시글 컨트롤러 (인증 필수)
 *
 * 로그인한 사용자만 접근 가능한 게시글 관리 API
 * - 게시글 생성, 수정, 삭제, 복구
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class PostUserController implements PostUserControllerDocs {

    private final PostService postService;

    // ========== CRUD ========== //

    /**
     * 게시글 생성
     * POST /api/user/posts
     * Content-Type: multipart/form-data
     * - request: JSON 파트 (제목, 본문 등)
     * - thumbnail: 이미지 파일 파트 (선택)
     */
    @Override
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<PostResponse.Detail>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") @Valid PostRequest.Create request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        PostResponse.Detail response = postService.create(userDetails.getUser(), request, thumbnail);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomResponse.success(response, "게시글이 생성되었습니다"));
    }

    /**
     * 게시글 수정용 데이터 조회
     * GET /api/user/posts/{slug}
     */
    @Override
    @GetMapping("/posts/{slug}")
    public ResponseEntity<CustomResponse<PostResponse.Edit>> getPostForEdit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug
    ) {
        PostResponse.Edit response = postService.getEdit(userDetails.getUserId(), slug);
        return ResponseEntity.ok(CustomResponse.success(response));
    }

    /**
     * 게시글 수정
     * PUT /api/user/posts/{postId}
     * Content-Type: multipart/form-data
     * - request: JSON 파트 (제목, 본문 등)
     * - thumbnail: 이미지 파일 파트 (선택, 없으면 기존 썸네일 유지)
     */
    @Override
    @PutMapping(value = "/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<PostResponse.Detail>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestPart("request") @Valid PostRequest.Update request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        PostResponse.Detail response = postService.update(userDetails.getUserId(), postId, request, thumbnail);
        return ResponseEntity.ok(CustomResponse.success(response, "게시글이 수정되었습니다"));
    }

    /**
     * 게시글 삭제 (소프트 삭제)
     * DELETE /api/user/posts/{postId}
     */
    @Override
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.delete(userDetails.getUserId(), postId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 게시글 복구 (소프트 삭제 취소)
     * PATCH /api/user/posts/{postId}/restore
     */
    @Override
    @PatchMapping("/posts/{postId}/restore")
    public ResponseEntity<CustomResponse<Void>> restore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.restore(userDetails.getUserId(), postId);
        return ResponseEntity.ok(CustomResponse.success(null, "게시글이 복구되었습니다"));
    }

    // ========== 조회 ========== //

    /**
     * 삭제된 게시글 목록 조회
     * GET /api/user/posts/deleted
     */
    @Override
    @GetMapping("/posts/deleted")
    public ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> getDeleted(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "deletedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItems> posts = postService.getDeleted(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(posts)));
    }
}
