package com.sealog.backend.domain.feature.post.util;

import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PostHtmlSanitizer 단위 테스트")
class PostHtmlSanitizerUnitTest extends UnitTest {

    @Nested
    @DisplayName("null/blank 입력 처리")
    class NullOrBlankInput {

        @Test
        @DisplayName("null 입력 시 null을 반환한다.")
        void null_입력() {
            assertThat(PostHtmlSanitizer.sanitize(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 입력 시 빈 문자열을 반환한다.")
        void 빈문자열_입력() {
            assertThat(PostHtmlSanitizer.sanitize("")).isBlank();
        }

        @Test
        @DisplayName("공백만 있는 입력 시 예외 없이 반환한다.")
        void 공백_입력() {
            assertThatCode(() -> PostHtmlSanitizer.sanitize("   "))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("성공: 허용된 태그와 속성은 유지된다.")
    void sanitize_Success() {
        // given
        String html = "<div data-type=\"post\">" +
                "<h1>제목</h1>" +
                "<p>내용</p>" +
                "<img src=\"https://example.com/test.jpg\" data-file-id=\"101\">" +
                "<video src=\"https://example.com/v.mp4\" controls=\"\"></video>" +
                "</div>";

        // when
        String result = PostHtmlSanitizer.sanitize(html);

        // then
        assertThat(result).contains("data-type=\"post\"");
        assertThat(result).contains("<h1>제목</h1>");
        assertThat(result).contains("src=\"https://example.com/test.jpg\"");
        assertThat(result).contains("data-file-id=\"101\"");
        assertThat(result).contains("video");
        assertThat(result).contains("src=\"https://example.com/v.mp4\"");
    }

    @Test
    @DisplayName("실패: 허용되지 않은 태그(script, iframe 등)가 포함되면 예외가 발생한다.")
    void sanitize_Fail_InvalidTags() {
        // given
        String scriptHtml = "<p>내용</p><script>alert('xss')</script>";
        String iframeHtml = "<div><iframe src='https://malicious.com'></iframe></div>";

        // when & then
        assertThatThrownBy(() -> PostHtmlSanitizer.sanitize(scriptHtml))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> PostHtmlSanitizer.sanitize(iframeHtml))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("정제: 허용되지 않은 속성(onclick 등)은 자동으로 제거된다.")
    void sanitize_StripAttributes() {
        // given
        String html = "<p onclick=\"alert(1)\">내용</p><img src=\"https://example.com/a.jpg\" onload=\"alert(1)\">";

        // when
        String result = PostHtmlSanitizer.sanitize(html);

        // then
        assertThat(result).contains("<p>내용</p>");
        assertThat(result).contains("src=\"https://example.com/a.jpg\"");
        assertThat(result).doesNotContain("onclick=");
        assertThat(result).doesNotContain("onload=");
    }
}
