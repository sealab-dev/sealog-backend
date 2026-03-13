package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Post", description = "공개 게시글 API (인증 불필요)")
@SecurityRequirements()
public interface PostGuestControllerDocs {

    @Operation(summary = "게시글 상세 조회", description = "Nickname + Slug로 공개 게시글 상세를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<CustomResponse<PostResponse.Detail>> getPostByNicknameAndSlug(
            @Parameter(description = "작성자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "게시글 slug", example = "spring-boot-jpa") String slug
    );

    @Operation(summary = "유저의 게시글 목록 조회", description = "nickname 기준으로 PUBLISHED 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> getUserPosts(
            @Parameter(description = "사용자 닉네임", example = "테스터") String nickname,
            Pageable pageable
    );

    @Operation(summary = "전체 게시글 목록 조회", description = "PUBLISHED 상태의 전체 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> getPosts(
            Pageable pageable
    );

    @Operation(summary = "스택별 게시글 목록 조회", description = "stackName으로 PUBLISHED 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> getPostsByStack(
            @Parameter(description = "스택 이름", example = "Java") String stackName,
            Pageable pageable
    );

    @Operation(summary = "공개 게시글 검색", description = "keyword로 PUBLISHED 게시글을 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> searchPosts(
            @Parameter(description = "검색 키워드", example = "spring") String keyword,
            Pageable pageable
    );
}
