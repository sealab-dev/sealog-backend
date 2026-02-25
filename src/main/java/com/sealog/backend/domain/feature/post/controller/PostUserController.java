package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.feature.post.dto.PostRequest;
import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.dto.PostSearchCondition;
import com.sealog.backend.domain.feature.post.enums.PostType;
import com.sealog.backend.domain.feature.post.service.PostUserService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
/**
 * 내 게시글 컨트롤러 (인증 필수)
 *
 * 로그인한 사용자만 접근 가능한 게시글 관리 API
 * - 게시글 생성, 수정, 삭제, 복구
 */

@RestController
@RequestMapping("/api/user/post")
@RequiredArgsConstructor
public class PostUserController implements PostUserControllerDocs {

    private final PostUserService postUserService;

    // ========== CRUD ========== //

    /**
     * 게시글 생성
     * POST /api/user/post
     *
     * @param request 게시글 데이터 (thumbnailFileId, thumbnailUrl 포함)
     */
    @Override
    @PostMapping
    public ResponseEntity<CustomResponse<PostResponse.Detail>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostRequest.Create request
    ) {
        PostResponse.Detail response = postUserService.create(
                userDetails.getUser(),
                request
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomResponse.success(response, "게시글이 생성되었습니다"));
    }

    /**
     * 게시글 수정용 데이터 조회
     * GET /api/user/post/{slug}/edit
     */
    @Override
    @GetMapping("/{slug}/edit")
    public ResponseEntity<CustomResponse<PostResponse.Edit>> getPostForEdit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug
    ) {
        PostResponse.Edit response = postUserService.getEdit(userDetails.getUserId(), slug);
        return ResponseEntity.ok(CustomResponse.success(response));
    }

    /**
     * 게시글 수정
     * PUT /api/user/post/{slug}
     *
     * @param slug 수정할 게시글 slug
     * @param request 게시글 데이터 (thumbnailFileId, thumbnailUrl, removeThumbnail 포함)
     */
    @Override
    @PutMapping("/{slug}")
    public ResponseEntity<CustomResponse<PostResponse.Detail>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug,
            @RequestBody @Valid PostRequest.Update request
    ) {
        PostResponse.Detail response = postUserService.update(
                userDetails.getUserId(),
                slug,
                request
        );
        return ResponseEntity.ok(CustomResponse.success(response, "게시글이 수정되었습니다"));
    }

    /**
     * 게시글 삭제 (소프트 삭제)
     * DELETE /api/user/post/{slug}
     *
     * @param slug 삭제할 게시글 slug
     */
    @Override
    @DeleteMapping("/{slug}")
    public ResponseEntity<CustomResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug
    ) {
        postUserService.delete(userDetails.getUserId(), slug);
        return ResponseEntity.ok(CustomResponse.success(null, "게시글이 삭제되었습니다"));
    }

    /**
     * 게시글 복구
     * POST /api/user/post/{slug}/restore
     *
     * @param slug 복구할 게시글 slug
     */
    @Override
    @PostMapping("/{slug}/restore")
    public ResponseEntity<CustomResponse<Void>> restore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug
    ) {
        postUserService.restore(userDetails.getUserId(), slug);
        return ResponseEntity.ok(CustomResponse.success(null, "게시글이 복구되었습니다"));
    }

    // ========== 조회 ========== //

    /**
     * 내 게시글 검색 (복합 필터링)
     * GET /api/user/post
     *
     * - DELETED 상태 제외 (삭제된 게시글은 별도 엔드포인트)
     */
    @Override
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> search(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false) String stack,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.ofMine(postType, stack, keyword);
        Page<PostResponse.PostItems> posts = postUserService.search(
                userDetails.getUserId(),
                condition,
                pageable
        );
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(posts)));
    }

    /**
     * 삭제된 게시글 목록 조회
     * GET /api/user/post/deleted
     */
    @Override
    @GetMapping("/deleted")
    public ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> getDeleted(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "deletedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItems> posts = postUserService.getDeleted(
                userDetails.getUserId(),
                pageable
        );
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(posts)));
    }
}