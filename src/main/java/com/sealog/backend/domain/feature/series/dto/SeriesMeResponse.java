package com.sealog.backend.domain.feature.series.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 내 시리즈 관련 응답 DTO
 */
public class SeriesMeResponse {

    @Getter
    @Builder
    @Schema(description = "내 시리즈 목록 정보")
    public static class MySeriesItem {
        private Long id;
        private String slug;
        private String name;
        private Boolean isPublic;

        public static MySeriesItem of(Long id, String slug, String name, Boolean isPublic) {
            return MySeriesItem.builder()
                    .id(id)
                    .slug(slug)
                    .name(name)
                    .isPublic(isPublic)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "내 시리즈 내 게시글 목록 정보")
    public static class MyPostItem {
        private Long postId;
        private String title;
        private String slug;
        private String thumbnailPath;
        private String status;

        public static MyPostItem of(Long postId, String title, String slug, String thumbnailPath, String status) {
            return MyPostItem.builder()
                    .postId(postId)
                    .title(title)
                    .slug(slug)
                    .thumbnailPath(thumbnailPath)
                    .status(status)
                    .build();
        }
    }
}
