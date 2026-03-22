package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.service.PostService;
import com.sealog.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 공개 게시글 컨트롤러 (인증 불필요)
 */
@Tag(name = "Post", description = "공개 게시글 API (인증 불필요) — PUBLISHED 상태 게시글만 조회됩니다.")
@SecurityRequirements()
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "전체 게시글 목록 조회",
            description = "PUBLISHED 상태의 전체 게시글을 최신순으로 페이지 조회합니다. 응답 data: `PageResponse<PostItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    useReturnTypeSchema = true),
    })
    @GetMapping("/posts")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> getPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> posts = postService.getPosts(pageable);
        return PageResponse.from(posts);
    }

    @Operation(
            summary = "특정 사용자의 게시글 목록 조회",
            description = "닉네임 기준으로 PUBLISHED 게시글 목록을 최신순으로 페이지 조회합니다. 응답 data: `PageResponse<PostItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/{nickname}/posts")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> getUserPosts(
            @Parameter(description = "사용자 닉네임", example = "seadev") @PathVariable String nickname,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> posts = postService.getUserPosts(nickname, pageable);
        return PageResponse.from(posts);
    }

    @Operation(
            summary = "특정 사용자의 카테고리별 게시글 목록 조회",
            description = "닉네임 + 카테고리명 기준으로 PUBLISHED 게시글 목록을 페이지 조회합니다. 응답 data: `PageResponse<PostItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping(value = "/{nickname}/posts", params = "categoryName")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> getPostsByCategory(
            @Parameter(description = "사용자 닉네임", example = "seadev") @PathVariable String nickname,
            @Parameter(description = "카테고리명 (CategoryItem.name)", example = "Spring Boot") @RequestParam String categoryName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> posts = postService.getPostsByCategory(nickname, categoryName, pageable);
        return PageResponse.from(posts);
    }

    @Operation(
            summary = "전체 공개 게시글 검색",
            description = "키워드로 전체 PUBLISHED 게시글을 제목·본문 기준으로 검색합니다. keyword 미입력 시 전체 목록 반환. 응답 data: `PageResponse<PostItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    useReturnTypeSchema = true),
    })
    @GetMapping("/posts/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> searchPosts(
            @Parameter(description = "검색 키워드 (미입력 시 전체 조회)", example = "spring")
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> results = postService.searchPosts(null, keyword, pageable);
        return PageResponse.from(results);
    }

    @Operation(
            summary = "특정 사용자의 공개 게시글 검색",
            description = "닉네임 기준으로 PUBLISHED 게시글을 키워드 검색합니다. 응답 data: `PageResponse<PostItem>`"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    useReturnTypeSchema = true),
    })
    @GetMapping("/{nickname}/posts/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> searchPostsByNickname(
            @Parameter(description = "사용자 닉네임", example = "seadev") @PathVariable String nickname,
            @Parameter(description = "검색 키워드", example = "spring")
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> results = postService.searchPostsByNickname(nickname, keyword, pageable);
        return PageResponse.from(results);
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "닉네임 + 슬러그로 PUBLISHED 게시글 상세 정보를 조회합니다. 응답 data: `PostDetail` (본문 content 포함)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.PostDetail.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음 또는 비공개",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/{nickname}/posts/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PostResponse.PostDetail getPostByNicknameAndSlug(
            @Parameter(description = "작성자 닉네임", example = "seadev") @PathVariable String nickname,
            @Parameter(description = "게시글 슬러그", example = "spring-boot-jpa-tips") @PathVariable String slug
    ) {
        return postService.getDetail(nickname, slug);
    }
}
