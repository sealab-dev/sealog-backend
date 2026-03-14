package com.sealog.backend.domain.feature.series.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.UtilityClass;


/**
 * Series Entity 관련 응답 DTO
 */
@UtilityClass
public class SeriesResponse {


    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "아카이브 목록 조회")
    public static class SeriesItems {

        private Long id;
        private String slug;
        private String name;

        public static SeriesItems of(Long id, String slug, String name) {

            return SeriesItems.builder()
                    .id(id)
                    .slug(slug)
                    .name(name)
                    .build();
        }
    }


    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(name = "SeriesPostItem", description = "아카이브에 속하는 블로그 게시글 목록 조회")
    public static class PostItems {

        private Long postId;
        private String title;
        private String slug;
        private String thumbnailPath;

        public static PostItems of(Long postId, String title, String slug, String thumbnailPath) {

            return PostItems.builder()
                    .postId(postId)
                    .title(title)
                    .slug(slug)
                    .thumbnailPath(thumbnailPath)
                    .build();
        }
    }
}