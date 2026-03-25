package com.sealog.backend.domain.feature.post.util;

import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PostHtmlParser 단위 테스트")
class PostHtmlParserUnitTest extends UnitTest {

    @Test
    @DisplayName("파일 ID 추출: HTML 본문에서 data-file-id 속성값을 모두 추출할 수 있다.")
    void extractFileIds_Success() {
        // given
        String html = """
                <p>Hello</p>
                <img src="/old/1.jpg" data-file-id="101">
                <video data-file-id="202"></video>
                <img data-file-id="invalid">
                """;

        // when
        Set<Long> fileIds = PostHtmlParser.extractFileIds(html);

        // then
        assertThat(fileIds).containsExactlyInAnyOrder(101L, 202L);
    }

    @Test
    @DisplayName("요약문 추출: HTML 태그를 제거하고 순수 텍스트만 최대 100자까지 추출한다.")
    void extractExcerpt_Success() {
        // given
        String html = "<div><p>가나다라마바사</p><span>아자차카타파하</span></div>";
        String longHtml = "<p>" + "a".repeat(150) + "</p>";

        // when & then
        assertThat(PostHtmlParser.extractExcerpt(html)).isEqualTo("가나다라마바사 아자차카타파하");
        assertThat(PostHtmlParser.extractExcerpt(longHtml)).hasSize(100);
        assertThat(PostHtmlParser.extractExcerpt(null)).isEmpty();
    }

    @Test
    @DisplayName("src 속성 제거: img와 video 태그의 src 속성을 제거한다.")
    void stripSrcAttributes_Success() {
        // given
        String html = "<img src='http://test.com/img.jpg' data-file-id='1'><video src='v.mp4'></video>";

        // when
        String result = PostHtmlParser.stripSrcAttributes(html);

        // then
        assertThat(result).doesNotContain("src=");
        assertThat(result).contains("data-file-id=\"1\"");
    }

    @Nested
    @DisplayName("processForStorage: src 제거 + 유효하지 않은 파일 태그 제거")
    class ProcessForStorage {

        @Test
        @DisplayName("src 속성 제거 및 유효하지 않은 파일 ID 태그가 제거된다.")
        void processForStorage_Success() {
            // given
            String html = "<img src=\"https://example.com/1.jpg\" data-file-id=\"1\">" +
                          "<img src=\"https://example.com/2.jpg\" data-file-id=\"2\">" +
                          "<video src=\"https://example.com/v.mp4\" data-file-id=\"3\"></video>";
            Set<Long> invalidIds = Set.of(2L);

            // when
            String result = PostHtmlParser.processForStorage(html, invalidIds);

            // then
            assertThat(result).doesNotContain("src=");
            assertThat(result).contains("data-file-id=\"1\"");
            assertThat(result).doesNotContain("data-file-id=\"2\"");  // 유효하지 않으므로 제거
            assertThat(result).contains("data-file-id=\"3\"");
        }

        @Test
        @DisplayName("유효하지 않은 ID 집합이 비어있어도 src만 제거된다.")
        void processForStorage_EmptyInvalidIds() {
            String html = "<img src=\"https://example.com/1.jpg\" data-file-id=\"1\">";

            String result = PostHtmlParser.processForStorage(html, Set.of());

            assertThat(result).doesNotContain("src=");
            assertThat(result).contains("data-file-id=\"1\"");
        }

        @Test
        @DisplayName("null 또는 빈 HTML 입력 시 그대로 반환한다.")
        void processForStorage_NullOrBlank() {
            assertThat(PostHtmlParser.processForStorage(null, Set.of())).isNull();
            assertThat(PostHtmlParser.processForStorage("", Set.of())).isBlank();
        }
    }

    @Nested
    @DisplayName("injectSrcAttributes: data-file-path로 src 재조립")
    class InjectSrcAttributes {

        @Test
        @DisplayName("data-file-path 속성을 기반으로 src를 올바르게 조립한다.")
        void injectSrcAttributes_Success() {
            // given
            String html = "<img data-file-path=\"/images/post/1.jpg\">" +
                          "<video data-file-path=\"videos/post/v.mp4\"></video>";
            String baseUrl = "https://cdn.example.com";

            // when
            String result = PostHtmlParser.injectSrcAttributes(html, baseUrl);

            // then
            assertThat(result).contains("src=\"https://cdn.example.com/images/post/1.jpg\"");
            assertThat(result).contains("src=\"https://cdn.example.com/videos/post/v.mp4\"");
        }

        @Test
        @DisplayName("baseUrl 끝에 슬래시가 있어도 중복 없이 src를 조립한다.")
        void injectSrcAttributes_TrailingSlash() {
            String html = "<img data-file-path=\"/images/1.jpg\">";

            String result = PostHtmlParser.injectSrcAttributes(html, "https://cdn.example.com/");

            assertThat(result).contains("src=\"https://cdn.example.com/images/1.jpg\"");
            assertThat(result).doesNotContain("//images");
        }
    }

    @Nested
    @DisplayName("removeFileTagsByIds: 유효하지 않은 파일 ID 태그 제거")
    class RemoveFileTagsByIds {

        @Test
        @DisplayName("지정한 파일 ID에 해당하는 img/video 태그를 제거한다.")
        void removeFileTagsByIds_Success() {
            String html = "<p>본문</p><img data-file-id=\"10\"><img data-file-id=\"20\">";
            Set<Long> invalidIds = Set.of(10L);

            String result = PostHtmlParser.removeFileTagsByIds(html, invalidIds);

            assertThat(result).contains("<p>본문</p>");
            assertThat(result).doesNotContain("data-file-id=\"10\"");
            assertThat(result).contains("data-file-id=\"20\"");
        }

        @Test
        @DisplayName("유효하지 않은 ID 집합이 비어있으면 원본 HTML을 그대로 반환한다.")
        void removeFileTagsByIds_EmptyInvalidIds() {
            String html = "<img data-file-id=\"10\">";

            String result = PostHtmlParser.removeFileTagsByIds(html, Set.of());

            assertThat(result).contains("data-file-id=\"10\"");
        }
    }

    @Nested
    @DisplayName("ContentPrep: prepare + finalize 파이프라인")
    class ContentPrepPipeline {

        @Test
        @DisplayName("prepare로 파일 ID를 추출하고, finalize로 src 제거 + 유효하지 않은 태그를 제거한다.")
        void prepare_and_finalize() {
            // given
            String html = "<img src=\"https://cdn.com/1.jpg\" data-file-id=\"1\">" +
                          "<img src=\"https://cdn.com/2.jpg\" data-file-id=\"2\">";

            // when
            PostHtmlParser.ContentPrep prep = PostHtmlParser.prepare(html);

            // then - 파일 ID 추출 확인
            assertThat(prep.getFileIds()).containsExactlyInAnyOrder(1L, 2L);

            // when - finalize: 유효하지 않은 ID(2L) 제거
            String result = prep.finalize(Set.of(2L));

            // then - src 제거, data-file-id=2 태그 제거
            assertThat(result).doesNotContain("src=");
            assertThat(result).contains("data-file-id=\"1\"");
            assertThat(result).doesNotContain("data-file-id=\"2\"");
        }

        @Test
        @DisplayName("finalize를 여러 번 호출해도 항상 동일한 결과를 반환한다. (멱등성)")
        void finalize_idempotent() {
            String html = "<img src=\"https://cdn.com/1.jpg\" data-file-id=\"1\">";
            PostHtmlParser.ContentPrep prep = PostHtmlParser.prepare(html);

            String first = prep.finalize(Set.of());
            String second = prep.finalize(Set.of());

            assertThat(first).isEqualTo(second);
        }
    }

    @Test
    @DisplayName("성능 테스트: 15만 자의 대용량 HTML 본문에서 요약문 및 파일 ID 추출 속도를 측정한다.")
    void performance_LargeHtmlParsing() {
        // given
        StringBuilder sb = new StringBuilder();
        sb.append("<p>");
        for (int i = 0; i < 10000; i++) {
            sb.append("이것은 테스트 문장입니다. ");
            if (i % 100 == 0) {
                sb.append("<img data-file-id=\"").append(i).append("\">");
            }
        }
        sb.append("</p>");
        String largeHtml = sb.toString(); // 약 15만 자 이상의 대용량 HTML
        
        long startTime = System.currentTimeMillis();

        // when
        String excerpt = PostHtmlParser.extractExcerpt(largeHtml);
        Set<Long> fileIds = PostHtmlParser.extractFileIds(largeHtml);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // then
        System.out.println("15만 자 파싱 소요 시간: " + duration + "ms");
        assertThat(excerpt).hasSize(100);
        assertThat(fileIds).isNotEmpty();
        assertThat(duration).isLessThan(500); // 500ms 이내 처리 권장
    }
}
