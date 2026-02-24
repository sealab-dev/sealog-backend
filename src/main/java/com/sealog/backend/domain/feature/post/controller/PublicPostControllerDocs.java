package com.sealog.backend.domain.feature.post.controller;

import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.entity.PostType;
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

import java.util.List;

@Tag(name = "Post", description = "공개 게시글 API (인증 불필요)")
@SecurityRequirements()
public interface PublicPostControllerDocs {

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

    @Operation(summary = "공개 게시글 검색", description = "PUBLISHED 게시글을 복합 조건으로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> searchPosts(
            @Parameter(description = "게시글 타입", example = "CORE") PostType postType,
            @Parameter(description = "스택명", example = "Spring Boot") String stack,
            @Parameter(description = "검색 키워드(제목/요약)", example = "JPA") String keyword,
            Pageable pageable
    );

    @Operation(summary = "특정 유저의 공개 게시글 조회", description = "nickname 기준으로 PUBLISHED 게시글을 복합 조건으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    ResponseEntity<CustomResponse<PageResponse<PostResponse.PostItems>>> getUserPublicPosts(
            @Parameter(description = "사용자 닉네임", example = "테스터") String nickname,
            @Parameter(description = "게시글 타입", example = "CORE") PostType postType,
            @Parameter(description = "스택명", example = "Spring Boot") String stack,
            @Parameter(description = "검색 키워드(제목/요약)", example = "JPA") String keyword,
            Pageable pageable
    );

    @Operation(summary = "게시글 자동완성", description = "keyword로 자동완성 검색(최대 10개)을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    ResponseEntity<CustomResponse<List<PostResponse.PostItems>>> autocomplete(
            @Parameter(description = "검색 키워드", example = "spring") String keyword
    );
}