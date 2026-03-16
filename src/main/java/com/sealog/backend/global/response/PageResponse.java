package com.sealog.backend.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@Schema(description = "페이지네이션 응답")
public class PageResponse<T> {

    @Schema(description = "현재 페이지 데이터 목록")
    private List<T> content;

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;

    @Schema(description = "전체 요소 수", example = "47")
    private long totalElements;

    @Schema(description = "현재 페이지 번호 (0-based)", example = "0")
    private int currentPage;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "이전 페이지 존재 여부", example = "false")
    private boolean hasPrevious;
    
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
