package com.sealog.backend.domain.feature.file.unit;

import com.sealog.backend.domain.feature.file.repository.FileMetadataRepository;
import com.sealog.backend.domain.feature.file.service.FileMetadataServiceImpl;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * FileMetadataService 단위 테스트
 */
@DisplayName("FileMetadataService 단위 테스트")
class FileMetadataServiceUnitTest extends UnitTest {

    @InjectMocks FileMetadataServiceImpl fileMetadataService;

    @Mock FileMetadataRepository fileMetadataRepository;

    // =====================================================================
    // 파일 메타데이터 단건 조회
    // =====================================================================
    @Nested
    @DisplayName("파일 메타데이터 조회 (getMetadata)")
    class 파일_메타데이터_조회 {

        @Test
        @DisplayName("파일 없음 → 404")
        void 파일_없음() {
            // given
            when(fileMetadataRepository.findById(99L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fileMetadataService.getMetadata(99L))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // =====================================================================
    // 파일 메타데이터 삭제
    // =====================================================================
    @Nested
    @DisplayName("파일 메타데이터 삭제 (remove)")
    class 파일_메타데이터_삭제 {

        @Test
        @DisplayName("파일 없음 → 404")
        void 파일_없음() {
            // given
            when(fileMetadataRepository.existsById(99L)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> fileMetadataService.remove(99L))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
