package com.sealog.backend.domain.feature.post.dto;

import com.sealog.backend.domain.feature.post.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 게시글 검색 조건
 * - 모든 조건은 optional (null이면 해당 조건 무시)
 */
@Getter
@Builder
@Schema(description = "게시글 검색 조건")
public class PostSearchCondition {

    @Schema(description = "게시글 타입", example = "CORE")
    private PostType postType;
    @Schema(description = "스택명", example = "Spring Boot")
    private String stackName;
    @Schema(description = "검색어", example = "JWT")
    private String keyword;
    @Schema(description = "작성자 닉네임", example = "테스터")
    private String nickname;  // 작성자 닉네임 필터

    /**
     * 공개 게시글 검색 조건 생성
     */
    public static PostSearchCondition ofPublic(PostType postType, String stackName, String keyword) {
        return PostSearchCondition.builder()
                .postType(postType)
                .stackName(stackName)
                .keyword(keyword)
                .build();
    }

    /**
     * 특정 사용자의 공개 게시글 검색 조건 생성
     */
    public static PostSearchCondition ofUser(String nickname, PostType postType, String stackName, String keyword) {
        return PostSearchCondition.builder()
                .nickname(nickname)
                .postType(postType)
                .stackName(stackName)
                .keyword(keyword)
                .build();
    }

    /**
     * 내 게시글 검색 조건 생성
     */
    public static PostSearchCondition ofMine(PostType postType, String stackName, String keyword) {
        return PostSearchCondition.builder()
                .postType(postType)
                .stackName(stackName)
                .keyword(keyword)
                .build();
    }
}