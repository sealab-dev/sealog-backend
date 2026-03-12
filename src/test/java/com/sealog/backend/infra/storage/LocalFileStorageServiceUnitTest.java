package com.sealog.backend.infra.storage;

import com.sealog.backend.infra.storage.service.LocalFileStorageService;
import com.sealog.backend.infra.storage.util.FileKeyGenerator;
import com.sealog.backend.support.base.TestUnitBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LocalFileStorageService 단위 테스트")
class LocalFileStorageServiceUnitTest extends TestUnitBase {

    // ========== application-test.yml 로딩 ========== //

    private static final Properties TEST_PROPS = loadTestProps();

    private static Properties loadTestProps() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("application-test.yml"));
        return factory.getObject();
    }

    private static final String BASE_URL = TEST_PROPS.getProperty("file.local.base-url");

    // ========== Mockito ========== //

    @Mock FileKeyGenerator fileKeyGenerator;

    @InjectMocks
    LocalFileStorageService localFileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // 파일 시스템 테스트를 위해 uploadDir을 임시 디렉토리로, baseUrl은 yml 값으로 주입
        ReflectionTestUtils.setField(localFileStorageService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(localFileStorageService, "baseUrl", BASE_URL);
    }

    // =====================================================================
    // getBaseUrl
    // =====================================================================
    @Nested
    @DisplayName("getBaseUrl")
    class GetBaseUrl {

        @Test
        @DisplayName("성공 - application-test.yml의 base-url을 그대로 반환한다")
        void 성공() {
            assertThat(localFileStorageService.getBaseUrl()).isEqualTo(BASE_URL);
        }
    }

    // =====================================================================
    // getFileUrl
    // =====================================================================
    @Nested
    @DisplayName("getFileUrl")
    class GetFileUrl {

        @Test
        @DisplayName("null 키 - null을 반환한다")
        void null_키_null_반환() {
            assertThat(localFileStorageService.getFileUrl(null)).isNull();
        }

        @Test
        @DisplayName("빈 키 - null을 반환한다")
        void 빈_키_null_반환() {
            assertThat(localFileStorageService.getFileUrl("   ")).isNull();
        }

        @Test
        @DisplayName("성공 - 정상 키를 baseUrl + '/' + key 형태의 URL로 반환한다")
        void 성공_정상_키() {
            String url = localFileStorageService.getFileUrl("public/images/2026/01/21/abc.jpg");
            assertThat(url).isEqualTo(BASE_URL + "/public/images/2026/01/21/abc.jpg");
        }

        @Test
        @DisplayName("성공 - '/'로 시작하는 키는 슬래시 중복 없이 URL을 반환한다")
        void 성공_슬래시로_시작하는_키() {
            String url = localFileStorageService.getFileUrl("/public/images/2026/01/21/abc.jpg");
            assertThat(url).isEqualTo(BASE_URL + "/public/images/2026/01/21/abc.jpg");
        }
    }

    // =====================================================================
    // getPresignedUrl
    // =====================================================================
    @Nested
    @DisplayName("getPresignedUrl")
    class GetPresignedUrl {

        @Test
        @DisplayName("성공 - 로컬 환경에서는 getFileUrl과 동일한 URL을 반환한다")
        void 성공_getFileUrl과_동일한_결과() {
            String fileKey = "public/images/2026/01/21/abc.jpg";
            assertThat(localFileStorageService.getPresignedUrl(fileKey, 60))
                    .isEqualTo(localFileStorageService.getFileUrl(fileKey));
        }

        @Test
        @DisplayName("null 키 - null을 반환한다")
        void null_키_null_반환() {
            assertThat(localFileStorageService.getPresignedUrl(null, 60)).isNull();
        }
    }

    // =====================================================================
    // deleteFile
    // =====================================================================
    @Nested
    @DisplayName("deleteFile")
    class DeleteFile {

        @Test
        @DisplayName("예외없음 - null 키는 조용히 무시한다")
        void null_키_예외없이_종료() {
            assertThatCode(() -> localFileStorageService.deleteFile(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("예외없음 - 공백 키는 조용히 무시한다")
        void 빈_키_예외없이_종료() {
            assertThatCode(() -> localFileStorageService.deleteFile("   "))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("성공 - 존재하는 파일을 삭제한다")
        void 성공_존재하는_파일_삭제() throws IOException {
            // given: @TempDir 내 실제 파일 생성
            Path testFile = tempDir.resolve("test.jpg");
            Files.createFile(testFile);

            // when
            localFileStorageService.deleteFile("test.jpg");

            // then: 실제로 파일이 사라졌는지 검증
            assertThat(Files.exists(testFile)).isFalse();
        }

        @Test
        @DisplayName("예외없음 - 존재하지 않는 파일은 경고만 남기고 종료한다")
        void 존재하지_않는_파일_예외없이_종료() {
            assertThatCode(() -> localFileStorageService.deleteFile("notexist.jpg"))
                    .doesNotThrowAnyException();
        }
    }

    // =====================================================================
    // deleteFiles
    // =====================================================================
    @Nested
    @DisplayName("deleteFiles")
    class DeleteFiles {

        @Test
        @DisplayName("빈 결과 - null 목록은 빈 리스트를 반환한다")
        void null_목록_빈_리스트_반환() {
            assertThat(localFileStorageService.deleteFiles(null)).isEmpty();
        }

        @Test
        @DisplayName("빈 결과 - 빈 목록은 빈 리스트를 반환한다")
        void 빈_목록_빈_리스트_반환() {
            assertThat(localFileStorageService.deleteFiles(List.of())).isEmpty();
        }

        @Test
        @DisplayName("빈 결과 - null/blank 키만 있는 목록은 빈 리스트를 반환한다")
        void null_blank만_있는_목록_빈_리스트_반환() {
            List<String> keys = new ArrayList<>();
            keys.add(null);
            keys.add("   ");

            assertThat(localFileStorageService.deleteFiles(keys)).isEmpty();
        }

        @Test
        @DisplayName("성공 - null/blank 키가 섞인 목록에서 유효한 키만 삭제하고 반환한다")
        void 성공_null_blank_섞인_목록_유효_키만_처리() throws IOException {
            // given: @TempDir 내 실제 파일 생성
            Path validFile = tempDir.resolve("valid.jpg");
            Files.createFile(validFile);

            List<String> keys = new ArrayList<>();
            keys.add(null);
            keys.add("   ");
            keys.add("valid.jpg");

            // when
            List<String> deleted = localFileStorageService.deleteFiles(keys);

            // then
            assertThat(deleted).containsExactly("valid.jpg");
            assertThat(Files.exists(validFile)).isFalse();
        }

        @Test
        @DisplayName("성공 - 여러 파일을 일괄 삭제하고 삭제된 키 목록을 반환한다")
        void 성공_여러_파일_일괄_삭제() throws IOException {
            Path file1 = tempDir.resolve("file1.jpg");
            Path file2 = tempDir.resolve("file2.jpg");
            Files.createFile(file1);
            Files.createFile(file2);

            List<String> deleted = localFileStorageService.deleteFiles(List.of("file1.jpg", "file2.jpg"));

            assertThat(deleted).containsExactlyInAnyOrder("file1.jpg", "file2.jpg");
            assertThat(Files.exists(file1)).isFalse();
            assertThat(Files.exists(file2)).isFalse();
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 파일도 성공 처리하여 키 목록에 포함한다")
        void 성공_존재하지_않는_파일도_성공_처리() {
            List<String> deleted = localFileStorageService.deleteFiles(List.of("notexist1.jpg", "notexist2.jpg"));

            assertThat(deleted).containsExactlyInAnyOrder("notexist1.jpg", "notexist2.jpg");
        }
    }
}
