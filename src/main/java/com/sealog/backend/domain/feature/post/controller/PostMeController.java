package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.base.validation.annotation.CheckFile;
import com.sealog.backend.domain.base.validation.enums.AllowedFileType;
import com.sealog.backend.domain.feature.post.dto.PostMeResponse;
import com.sealog.backend.domain.feature.post.dto.PostMeRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/me/posts")
@RequiredArgsConstructor
public class PostMeController implements PostMeControllerDocs {

    private final PostService postService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostMeResponse.MyPostItem create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") PostMeRequest.Create request,
            @CheckFile(allowed = {AllowedFileType.IMAGE}, maxSizeMB = 10, nullable = true)
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        return postService.create(userDetails.getUser(), request, thumbnail);
    }

    @Override
    @GetMapping("/edit/{slug}")
    @ResponseStatus(HttpStatus.OK)
    public PostMeResponse.MyPostEdit getPostForEdit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String slug
    ) {
        return postService.getEdit(userDetails.getUserId(), slug);
    }

    @Override
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostMeResponse.MyPostItem update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestPart("request") PostMeRequest.Update request,
            @CheckFile(allowed = {AllowedFileType.IMAGE}, maxSizeMB = 10, nullable = true)
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        return postService.update(userDetails.getUserId(), postId, request, thumbnail);
    }

    @Override
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.delete(userDetails.getUserId(), postId);
        return CustomResponse.success("게시글이 삭제되었습니다");
    }

    @Override
    @PatchMapping("/{postId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CustomResponse<Void> restore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.restore(userDetails.getUserId(), postId);
        return CustomResponse.success("게시글이 복구되었습니다");
    }

    @Override
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PostMeResponse.MyPostItem> searchPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostMeResponse.MyPostItem> results = postService.searchMyPosts(userDetails.getUser().getNickname(), keyword, pageable);
        return PageResponse.from(results);
    }

    @Override
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
