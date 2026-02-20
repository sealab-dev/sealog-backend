package com.sealog.backend.global.file.controller;

import com.sealog.backend.global.core.response.CustomResponse;
import com.sealog.backend.global.file.dto.FileUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일 업로드 API (Swagger 문서 전용)
 *
 * - 컨트롤러 구현체는 비즈니스 로직만 담당하고,
 * - Swagger/OpenAPI 문서화 관련 어노테이션은 이 인터페이스에만 모아 관리합니다.
 */
@Tag(name = "File", description = "파일 업로드 API")
@SecurityRequirement(name = "bearerAuth")
public interface FileUploadControllerDocs {

    @Operation(
            summary = "파일 업로드",
            description = "파일을 업로드하고 메타데이터를 저장한 뒤 업로드 결과를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "파일 검증 실패(확장자/크기/MIME 타입 등)"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<CustomResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "업로드할 파일", required = true,
                    content = @Content(mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("file") MultipartFile file
    ) throws IOException;
}
