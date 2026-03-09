package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.service.PostService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공개 게시글 컨트롤러 (인증 불필요)
 *
 * 모든 사용자가 접근 가능한 공개 게시글 관련 API
 * - PUBLISHED 상태의 게시글만 조회 가능
 */
@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
public class PostGuestController implements PostGuestControllerDocs {

    private final PostService postService;

    /**
     * 게시글 상세 조회 (Nickname + Slug 기반)
     * GET /api/guest/{nickname}/posts/{slug}
     */
    @Override
    @GetMapping("/{nickname}/posts/{slug}")
    public ResponseEntity<CustomResponse<PostResponse.Detail>> getPostByNicknameAndSlug(
            @PathVariable String nickname,
            @PathVariable String slug
    ) {
        PostResponse.Detail response = postService.getDetail(nickname, slug);
        return ResponseEntity.ok(CustomResponse.success(response));
    }

    /**
     * 특정 사용자의 공개 게시글 목록 조회
     * GET /api/guest/{nickname}/posts
     */
    @Override
    @GetMapping("/{nickname}/posts")
    public ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> getUserPosts(
            @PathVariable String nickname,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItems> posts = postService.getUserPosts(nickname, pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(posts)));
    }

    /**
     * 전체 공개 게시글 목록 조회
     * GET /api/guest/posts
     */
    @Override
    @GetMapping("/posts")
    public ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> getPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItems> posts = postService.getPosts(pageable);
        return ResponseEntity.ok(CustomResponse.success(PageResponse.from(posts)));
    }

    /**
     * 게시글 자동완성 검색
     * GET /api/guest/posts/autocomplete?keyword=검색어
     *
     * - PUBLISHED 상태만 검색
     * - 최대 10개 반환
     */
    @Override
    @GetMapping("/posts/autocomplete")
    public ResponseEntity<CustomResponse<List<PostResponse.PostItems>>> autocomplete(
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        List<PostResponse.PostItems> results = postService.autocomplete(keyword);
        return ResponseEntity.ok(CustomResponse.success(results));
    }
}
