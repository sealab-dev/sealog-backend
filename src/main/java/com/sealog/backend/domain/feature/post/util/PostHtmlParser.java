package com.sealog.backend.domain.feature.post.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class PostHtmlParser {

    private PostHtmlParser() {}

    /**
     * HTML 본문에서 <img data-file-id="..."> 속성을 추출합니다.
     *
     * @param html 파싱할 HTML 문자열
     * @return 파일 ID Set (중복 제거)
     */
    public static Set<Long> extractFileIds(String html) {
        Set<Long> fileIds = new HashSet<>();

        if (html == null || html.isBlank()) {
            log.debug("본문이 비어있음 - 파일 ID 없음");
            return fileIds;
        }

        Document doc = Jsoup.parseBodyFragment(html);
        Elements images = doc.select("img[data-file-id]");

        for (Element img : images) {
            String fileIdStr = img.attr("data-file-id");
            try {
                fileIds.add(Long.parseLong(fileIdStr));
            } catch (NumberFormatException e) {
                log.warn("data-file-id 파싱 실패: value={}", fileIdStr);
            }
        }

        log.debug("HTML에서 파일 ID 추출 완료: count={}, ids={}", fileIds.size(), fileIds);
        return fileIds;
    }

    /**
     * DB 저장 전: <img> 태그의 src 속성을 제거합니다.
     * data-file-path만 남겨 저장합니다.
     *
     * @param html 원본 HTML
     * @return src 속성이 제거된 HTML
     */
    public static String stripSrcAttributes(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }

        Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings().prettyPrint(false);

        Elements images = doc.select("img[src]");
        for (Element img : images) {
            img.removeAttr("src");
        }

        log.debug("src 속성 제거 완료: 대상 img 수={}", images.size());
        return doc.body().html();
    }

    /**
     * 조회 시: <img data-file-path="..."> 속성을 이용해 src를 재조립합니다.
     *
     * @param html     DB에서 조회한 HTML (src 없음)
     * @param baseUrl  storage.base-url (예: https://cdn.example.com)
     * @return src가 채워진 HTML
     */
    public static String injectSrcAttributes(String html, String baseUrl) {
        if (html == null || html.isBlank()) {
            return html;
        }

        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings().prettyPrint(false);

        Elements images = doc.select("img[data-file-path]");
        for (Element img : images) {
            String filePath = img.attr("data-file-path");
            String src = filePath.startsWith("/") ? base + filePath : base + "/" + filePath;
            img.attr("src", src);
        }

        log.debug("src 속성 주입 완료: 대상 img 수={}", images.size());
        return doc.body().html();
    }
}
