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
@Tag(name = "Post", description = "공개 게시글 API (인증 불필요)")
@SecurityRequirements()
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "전체 게시글 목록 조회", description = "PUBLISHED 상태의 전체 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/posts")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> getPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> posts = postService.getPosts(pageable);
        return PageResponse.from(posts);
    }

    @Operation(summary = "유저의 게시글 목록 조회", description = "nickname 기준으로 PUBLISHED 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/{nickname}/posts")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> getUserPosts(
            @Parameter(description = "사용자 닉네임", example = "테스터") @PathVariable String nickname,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> posts = postService.getUserPosts(nickname, pageable);
        return PageResponse.from(posts);
    }

    @Operation(summary = "유저의 스택별 게시글 목록 조회", description = "nickname 기준으로 stackName에 해당하는 PUBLISHED 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping(value = "/{nickname}/posts", params = "stackName")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> getPostsByStack(
            @Parameter(description = "사용자 닉네임", example = "테스터") @PathVariable String nickname,
            @Parameter(description = "스택 이름", example = "Java") @RequestParam String stackName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> posts = postService.getPostsByStack(nickname, stackName, pageable);
        return PageResponse.from(posts);
    }

    @Operation(summary = "공개 게시글 검색", description = "keyword로 전체 PUBLISHED 게시글을 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/posts/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> searchPosts(
            @Parameter(description = "검색 키워드", example = "spring")
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> results = postService.searchPosts(null, keyword, pageable);
        return PageResponse.from(results);
    }

    @Operation(summary = "유저의 공개 게시글 검색", description = "nickname 기준으로 keyword에 해당하는 PUBLISHED 게시글을 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/{nickname}/posts/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> searchPostsByNickname(
            @Parameter(description = "사용자 닉네임", example = "테스터") @PathVariable String nickname,
            @Parameter(description = "검색 키워드", example = "spring")
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> results = postService.searchPostsByNickname(nickname, keyword, pageable);
        return PageResponse.from(results);
    }

    @Operation(summary = "게시글 상세 조회", description = "Nickname + Slug로 공개 게시글 상세를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/{nickname}/posts/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PostResponse.PostDetail getPostByNicknameAndSlug(
            @Parameter(description = "작성자 닉네임", example = "테스터") @PathVariable String nickname,
            @Parameter(description = "게시글 slug", example = "spring-boot-jpa") @PathVariable String slug
    ) {
        return postService.getDetail(nickname, slug);
    }
}
