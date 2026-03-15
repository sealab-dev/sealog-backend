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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 공개 게시글 컨트롤러 (인증 불필요)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController implements PostControllerDocs {

    private final PostService postService;

    @Override
    @GetMapping("/posts")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> getPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> posts = postService.getPosts(pageable);
        return PageResponse.from(posts);
    }

    @Override
    @GetMapping("/{nickname}/posts")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> getUserPosts(
            @PathVariable String nickname,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> posts = postService.getUserPosts(nickname, pageable);
        return PageResponse.from(posts);
    }

    @Override
    @GetMapping(value = "/{nickname}/posts", params = "stackName")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> getPostsByStack(
            @PathVariable String nickname,
            @RequestParam String stackName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> posts = postService.getPostsByStack(nickname, stackName, pageable);
        return PageResponse.from(posts);
    }

    @Override
    @GetMapping("/posts/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> searchPosts(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> results = postService.searchPosts(null, keyword, pageable);
        return PageResponse.from(results);
    }

    @Override
    @GetMapping("/{nickname}/posts/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostResponse.PostItem> searchPostsByNickname(
            @PathVariable String nickname,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse.PostItem> results = postService.searchPostsByNickname(nickname, keyword, pageable);
        return PageResponse.from(results);
    }

    @Override
    @GetMapping("/{nickname}/posts/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PostResponse.PostDetail getPostByNicknameAndSlug(
            @PathVariable String nickname,
            @PathVariable String slug
    ) {
        return postService.getDetail(nickname, slug);
    }

}
