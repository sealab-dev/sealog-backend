package com.sealog.backend.domain.base.validation.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 파일 타입별 허용 MIME 타입 및 확장자 정의 (Apache Tika 2.9.1 canonical 기준)
 *
 * <p>확장자 검증은 텍스트 기반 파일 등 매직 바이트만으로 구분이 어려운 경우를
 * 보완하기 위해 병행합니다.</p>
 *
 * @see <a href="https://github.com/apache/tika/blob/2.9.1/tika-core/src/main/resources/org/apache/tika/mime/tika-mimetypes.xml">tika-mimetypes.xml</a>
 */
@UtilityClass
public class AllowedFileConstant {

    public static final String MIME = "MIME";
    public static final String EXT = "EXT";


    public static final Set<String> MIME_IMAGE = Set.of(
            "image/jpeg",       // .jpg, .jpeg
            "image/png",        // .png
            "image/gif",        // .gif
            "image/webp",       // .webp
            "image/svg+xml",    // .svg
            "image/bmp"         // .bmp
    );

    public static final Set<String> MIME_VIDEO = Set.of(
            "video/mp4",         // .mp4
            "video/mpeg",        // .mpeg
            "video/quicktime",   // .mov
            "video/x-msvideo",   // .avi
            "video/x-flv",       // .flv
            "video/webm",        // .webm
            "video/x-matroska"   // .mkv
    );

    public static final Set<String> MIME_DOCUMENT = Set.of(
            "application/pdf",                                                           // .pdf
            "application/msword",                                                        // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",   // .docx
            "application/vnd.ms-excel",                                                  // .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",         // .xlsx
            "application/vnd.ms-powerpoint",                                             // .ppt
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"  // .pptx
    );

    public static final Set<String> MIME_AUDIO = Set.of(
            "audio/mpeg",       // .mp3
            "audio/vnd.wave",   // .wav
            "audio/vorbis",     // .ogg
            "audio/x-flac",     // .flac
            "audio/x-aac",      // .aac
            "audio/mp4"         // .m4a
    );

    public static final Set<String> MIME_ARCHIVE = Set.of(
            "application/zip",              // .zip
            "application/x-rar-compressed",  // .rar
            "application/x-7z-compressed",   // .7z
            "application/x-tar",             // .tar
            "application/gzip"               // .gz
    );

    public static final Set<String> MIME_ALL = Stream.of(
            MIME_IMAGE, MIME_VIDEO, MIME_DOCUMENT, MIME_AUDIO, MIME_ARCHIVE
    ).flatMap(Set::stream).collect(Collectors.toUnmodifiableSet());


    public static final Set<String> EXT_IMAGE = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp"
    );

    public static final Set<String> EXT_VIDEO = Set.of(
            "mp4", "mpeg", "mov", "avi", "flv", "webm", "mkv"
    );

    public static final Set<String> EXT_DOCUMENT = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
    );

    public static final Set<String> EXT_AUDIO = Set.of(
            "mp3", "wav", "ogg", "flac", "aac", "m4a"
    );

    public static final Set<String> EXT_ARCHIVE = Set.of(
            "zip", "rar", "7z", "tar", "gz"
    );

    public static final Set<String> EXT_ALL = Stream.of(
            EXT_IMAGE, EXT_VIDEO, EXT_DOCUMENT, EXT_AUDIO, EXT_ARCHIVE
    ).flatMap(Set::stream).collect(Collectors.toUnmodifiableSet());

}