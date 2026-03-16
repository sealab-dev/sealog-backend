package com.sealog.backend.domain.feature.post.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class PostHtmlParser {

    private PostHtmlParser() {}

    /**
     * 본문 저장을 위한 사전 처리 컨텍스트.
     * Document를 살려두어 재파싱 없이 후속 처리를 수행합니다.
     */
    public static final class ContentPrep {

        private final Document doc;
        @Getter
        private final Set<Long> fileIds;

        private ContentPrep(Document doc, Set<Long> fileIds) {
            this.doc = doc;
            this.fileIds = Collections.unmodifiableSet(fileIds);
        }

        /**
         * src 제거와 유효하지 않은 파일 태그 제거를 동시에 수행하고 HTML을 반환합니다.
         * Document를 clone해 원본을 보존하므로 여러 번 호출해도 항상 같은 결과를 반환합니다.
         *
         * @param invalidIds 제거할 파일 ID 집합
         * @return 처리 완료된 HTML 문자열
         */
        public String finalize(Set<Long> invalidIds) {
            Document copy = doc.clone();
            for (Element img : copy.body().select("img")) {
                img.removeAttr("src");

                String fileIdStr = img.attr("data-file-id");
                if (!fileIdStr.isEmpty()) {
                    try {
                        if (!invalidIds.isEmpty() && invalidIds.contains(Long.parseLong(fileIdStr))) {
                            img.remove();
                        }
                    } catch (NumberFormatException e) {
                        log.warn("data-file-id 파싱 실패로 태그 제거: value={}", fileIdStr);
                        img.remove();
                    }
                }
            }
            return copy.body().html();
        }
    }

    /**
     * XSS 정제가 완료된 HTML을 단일 파싱으로 처리합니다.
     * PostHtmlSanitizer.sanitize() 호출 후 이 메서드를 호출하십시오.
     * 파일 ID 추출을 수행하고 Document를 보존합니다.
     * 반환된 ContentPrep.finalize(invalidIds)를 호출해 최종 HTML을 얻습니다.
     *
     * @param sanitizedHtml PostHtmlSanitizer.sanitize()가 반환한 정제된 HTML
     * @return ContentPrep (파일 ID 포함, Document 살아있음)
     */
    public static ContentPrep prepare(String sanitizedHtml) {
        if (sanitizedHtml == null || sanitizedHtml.isBlank()) {
            return new ContentPrep(Jsoup.parseBodyFragment(""), Set.of());
        }

        // 1. 파싱 (딱 한 번, sanitize 이후이므로 추가 검증 불필요)
        Document doc = Jsoup.parseBodyFragment(sanitizedHtml);
        doc.outputSettings().prettyPrint(false);

        // 2. 파일 ID 추출 (DOM 순회, 재파싱 없음)
        Set<Long> fileIds = new HashSet<>();
        for (Element img : doc.body().select("img[data-file-id]")) {
            try {
                fileIds.add(Long.parseLong(img.attr("data-file-id")));
            } catch (NumberFormatException e) {
                log.warn("data-file-id 파싱 실패: value={}", img.attr("data-file-id"));
            }
        }

        log.debug("본문 파싱 완료: fileIdCount={}", fileIds.size());
        return new ContentPrep(doc, fileIds);
    }

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
     * 유효하지 않은 파일 ID에 해당하는 <img data-file-id="..."> 태그를 제거합니다.
     * 존재하지 않거나 소유자가 다른 파일 참조를 본문에서 제거할 때 사용합니다.
     *
     * @param html           원본 HTML
     * @param invalidFileIds 제거 대상 파일 ID 집합
     * @return 유효하지 않은 img 태그가 제거된 HTML
     */
    public static String removeFileTagsByIds(String html, Set<Long> invalidFileIds) {
        if (html == null || html.isBlank() || invalidFileIds == null || invalidFileIds.isEmpty()) {
            return html;
        }

        Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings().prettyPrint(false);

        Elements images = doc.select("img[data-file-id]");
        int removedCount = 0;
        for (Element img : images) {
            try {
                Long fileId = Long.parseLong(img.attr("data-file-id"));
                if (invalidFileIds.contains(fileId)) {
                    img.remove();
                    removedCount++;
                }
            } catch (NumberFormatException e) {
                log.warn("data-file-id 파싱 실패로 태그 제거: value={}", img.attr("data-file-id"));
                img.remove();
                removedCount++;
            }
        }

        log.debug("유효하지 않은 파일 태그 제거 완료: removedCount={}", removedCount);
        return doc.body().html();
    }

    /**
     * DB 저장 전 처리: 단일 파싱으로 src 제거와 유효하지 않은 파일 태그 제거를 동시에 수행합니다.
     * sanitize 이후에 호출하며, invalidFileIds가 비어있어도 src 제거는 항상 수행합니다.
     *
     * @param sanitizedHtml  XSS 정제가 완료된 HTML
     * @param invalidFileIds 제거할 파일 ID 집합 (없으면 빈 Set)
     * @return src가 제거되고 유효하지 않은 img 태그가 삭제된 HTML
     */
    public static String processForStorage(String sanitizedHtml, Set<Long> invalidFileIds) {
        if (sanitizedHtml == null || sanitizedHtml.isBlank()) {
            return sanitizedHtml;
        }

        Document doc = Jsoup.parseBodyFragment(sanitizedHtml);
        doc.outputSettings().prettyPrint(false);

        int removedCount = 0;
        for (Element img : doc.select("img")) {
            img.removeAttr("src");

            String fileIdStr = img.attr("data-file-id");
            if (!fileIdStr.isEmpty() && !invalidFileIds.isEmpty()) {
                try {
                    if (invalidFileIds.contains(Long.parseLong(fileIdStr))) {
                        img.remove();
                        removedCount++;
                    }
                } catch (NumberFormatException e) {
                    log.warn("data-file-id 파싱 실패로 태그 제거: value={}", fileIdStr);
                    img.remove();
                    removedCount++;
                }
            }
        }

        log.debug("본문 저장 처리 완료: removedInvalidFileTags={}", removedCount);
        return doc.body().html();
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
     * @param baseUrl  file.local.base-url (예: https://cdn.example.com)
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
