package com.sealog.backend.domain.feature.file.controller;

import com.sealog.backend.domain.base.validation.annotation.CheckFile;
import com.sealog.backend.domain.base.validation.enums.AllowedFileType;
import com.sealog.backend.domain.feature.file.dto.FileResponse;
import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import com.sealog.backend.domain.feature.file.service.FileMetadataService;
import com.sealog.backend.infra.storage.dto.FileUploadResult;
import com.sealog.backend.infra.storage.service.FileStorageService;
import com.sealog.backend.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일 업로드 컨트롤러
 * - 파일 검증 (크기, 확장자, MIME 타입)
 * - S3 업로드 및 메타데이터 저장
 */
@Slf4j
@Tag(name = "File", description = "파일 업로드 API")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final FileMetadataService fileMetadataService;

    /**
     * 파일 업로드
     *
     * 처리 흐름:
     * 1. 파일 검증 (크기, 확장자, MIME 타입)
     * 2. S3에 파일 업로드 (타입별 경로 자동 분류)
     * 3. FileMetadata 생성 및 저장
     * 4. 업로드 결과 반환
     *
     * @param file 업로드할 파일
     * @return FileResponse 업로드된 파일 정보 (ID, URL 등)
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @Operation(
            summary = "파일 업로드",
            description = """
                    파일을 S3에 업로드하고 메타데이터를 저장한 뒤 업로드 결과를 반환합니다.

                    **제약 조건**
                    - 최대 파일 크기: 100MB
                    - 허용 파일 타입: 이미지 (jpg, jpeg, png, gif, webp, svg, bmp) / 영상 (mp4, mpeg, mov, avi, flv, webm, mkv)
                    - 파일 타입은 MIME 타입 및 확장자 이중 검증

                    **응답 data 필드**: `FileResponse` (id, originalName, path, fileUrl, size, contentType)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공",
                    content = @Content(schema = @Schema(implementation = FileResponse.class))),
            @ApiResponse(responseCode = "400", description = "파일 검증 실패 (확장자·크기·MIME 타입 오류)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 (S3 업로드 실패 등)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public FileResponse uploadFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "업로드할 파일 (multipart/form-data)", required = true,
                    content = @Content(mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary")))
            @CheckFile(allowed = {AllowedFileType.IMAGE, AllowedFileType.VIDEO}, maxSizeMB = 100)
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        log.debug("파일 업로드 요청: filename={}, contentType={}, size={}bytes",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        // 2. 업로드 (타입별 경로 자동 분류)
        FileUploadResult uploadResult = fileStorageService.uploadFile(file);
        log.debug("업로드 완료: originalName={} path={} contentType={}",
                uploadResult.originalName(), uploadResult.path(), uploadResult.contentType());

        // 3. FileMetadata 저장
        FileMetadata fileMetadata = fileMetadataService.upload(uploadResult, userDetails.getUser());
        log.debug("파일 메타데이터 저장 완료: fileId={}", fileMetadata.getId());

        // 4. 응답 반환
        String fileUrl = fileStorageService.getFileUrl(fileMetadata.getPath());
        FileResponse response = FileResponse.from(fileMetadata, fileUrl);
        log.debug("파일 업로드 성공: fileId={}, path={}", response.id(), response.path());

        return response;
    }
}
