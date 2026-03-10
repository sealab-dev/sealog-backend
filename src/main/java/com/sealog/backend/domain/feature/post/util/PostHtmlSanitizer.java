package com.sealog.backend.domain.feature.post.util;

import com.sealog.backend.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.util.Set;

@Slf4j
public class PostHtmlSanitizer {

    private PostHtmlSanitizer() {}

    /**
     * 허용 태그 화이트리스트 (Safelist.relaxed() 기반)
     *
     * 허용: a, b, blockquote, br, caption, cite, code, col, colgroup,
     *       dd, dfn, div, dl, dt, em, figcaption, figure, h1-h6, header,
     *       hr, i, img, li, ol, p, pre, q, small, span, strike, strong,
     *       sub, sup, table, tbody, td, tfoot, th, thead, tr, u, ul
     *
     * 차단: script, style, iframe, form, input 등 → 400 Bad Request
     */
    private static final Set<String> ALLOWED_TAGS = Set.of(
            "a", "b", "blockquote", "br", "caption", "cite", "code", "col", "colgroup",
            "dd", "dfn", "div", "dl", "dt", "em", "figcaption", "figure",
            "h1", "h2", "h3", "h4", "h5", "h6", "header", "hr", "i", "img",
            "li", "ol", "p", "pre", "q", "small", "span", "strike", "strong",
            "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "u", "ul"
    );

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addAttributes("img", "data-file-id", "data-file-path");

    private static final Document.OutputSettings OUTPUT_SETTINGS =
            new Document.OutputSettings().prettyPrint(false);

    /**
     * HTML 허용 태그를 검증 후 정제된 HTML을 반환합니다.
     * 허용되지 않은 태그(script, iframe 등)가 포함된 경우 예외를 던집니다.
     *
     * @param html 원본 HTML
     * @return 정제된 HTML
     * @throws CustomException 허용되지 않는 태그가 포함된 경우
     */
    public static String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }

        Document doc = Jsoup.parseBodyFragment(html);
        for (Element el : doc.body().getAllElements()) {
            String tag = el.tagName();
            if (!"body".equals(tag) && !ALLOWED_TAGS.contains(tag)) {
                log.warn("허용되지 않는 HTML 태그 감지: tag={}", tag);
                throw CustomException.badRequest("허용되지 않는 HTML 태그가 포함되어 있습니다");
            }
        }

        String cleaned = Jsoup.clean(html, "", SAFELIST, OUTPUT_SETTINGS);
        log.debug("HTML 새니타이즈 완료: 원본 길이={}, 결과 길이={}", html.length(), cleaned.length());
        return cleaned;
    }
}
