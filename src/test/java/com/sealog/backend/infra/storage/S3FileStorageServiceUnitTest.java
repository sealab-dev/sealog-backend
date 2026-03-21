package com.sealog.backend.infra.storage;

import com.sealog.backend.infra.storage.exception.FileStorageException;
import com.sealog.backend.infra.storage.service.S3FileStorageService;
import com.sealog.backend.infra.storage.util.FileKeyGenerator;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateInvalidationResponse;
import software.amazon.awssdk.services.cloudfront.model.Invalidation;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("S3FileStorageService 단위 테스트")
class S3FileStorageServiceUnitTest extends UnitTest {

    // ========== application-test.yml 로딩 ========== //

    private static final Properties TEST_PROPS = loadTestProps();

    private static Properties loadTestProps() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("application-test.yml"));
        return factory.getObject();
    }

    private static final String BUCKET          = TEST_PROPS.getProperty("spring.cloud.aws.s3.bucket");
    private static final String REGION          = TEST_PROPS.getProperty("spring.cloud.aws.region.static");
    private static final String CF_DOMAIN       = TEST_PROPS.getProperty("spring.cloud.aws.cloudfront.domain");
    private static final String DISTRIBUTION_ID = TEST_PROPS.getProperty("spring.cloud.aws.cloudfront.distribution-id");

    // ========== Mockito ========== //

    @Mock S3Client s3Client;
    @Mock S3Presigner s3Presigner;
    @Mock CloudFrontClient cloudFrontClient;
    @Mock FileKeyGenerator fileKeyGenerator;

    @InjectMocks
    S3FileStorageService s3FileStorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3FileStorageService, "bucketName", BUCKET);
        ReflectionTestUtils.setField(s3FileStorageService, "region", REGION);
        ReflectionTestUtils.setField(s3FileStorageService, "cloudFrontDomain", CF_DOMAIN);
        ReflectionTestUtils.setField(s3FileStorageService, "distributionId", DISTRIBUTION_ID);
    }

    // =====================================================================
    // getBaseUrl
    // =====================================================================
    @Nested
    @DisplayName("getBaseUrl")
    class GetBaseUrl {

        @Test
        @DisplayName("성공 - CloudFront 설정 시 CloudFront Base URL을 반환한다")
        void 성공_CloudFront_설정() {
            assertThat(s3FileStorageService.getBaseUrl())
                    .isEqualTo("https://" + CF_DOMAIN);
        }

        @Test
        @DisplayName("성공 - CloudFront 미설정 시 S3 Base URL을 반환한다")
        void 성공_CloudFront_미설정() {
            ReflectionTestUtils.setField(s3FileStorageService, "cloudFrontDomain", "");

            assertThat(s3FileStorageService.getBaseUrl())
                    .isEqualTo("https://" + BUCKET + ".s3." + REGION + ".amazonaws.com");
        }
    }

    // =====================================================================
    // getFileUrl
    // =====================================================================
    @Nested
    @DisplayName("getFileUrl")
    class GetFileUrl {

        @Test
        @DisplayName("실패 - null 키는 FileStorageException(400)을 발생시킨다")
        void 실패_null_키() {
            assertThatThrownBy(() -> s3FileStorageService.getFileUrl(null))
                    .isInstanceOf(FileStorageException.class)
                    .satisfies(ex -> assertThat(((FileStorageException) ex).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("실패 - 공백 키는 FileStorageException(400)을 발생시킨다")
        void 실패_공백_키() {
            assertThatThrownBy(() -> s3FileStorageService.getFileUrl("   "))
                    .isInstanceOf(FileStorageException.class)
                    .satisfies(ex -> assertThat(((FileStorageException) ex).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("성공 - CloudFront 설정 시 CloudFront URL을 반환한다")
        void 성공_CloudFront_설정() {
            String url = s3FileStorageService.getFileUrl("public/images/abc.jpg");

            assertThat(url).isEqualTo("https://" + CF_DOMAIN + "/public/images/abc.jpg");
        }

        @Test
        @DisplayName("성공 - CloudFront 미설정 시 S3 URL을 반환한다")
        void 성공_CloudFront_미설정() {
            ReflectionTestUtils.setField(s3FileStorageService, "cloudFrontDomain", "");

            String url = s3FileStorageService.getFileUrl("public/images/abc.jpg");

            assertThat(url).isEqualTo("https://" + BUCKET + ".s3." + REGION + ".amazonaws.com/public/images/abc.jpg");
        }

        @Test
        @DisplayName("성공 - '/'로 시작하는 키는 슬래시 중복 없이 URL을 반환한다")
        void 성공_슬래시로_시작하는_키() {
            String url = s3FileStorageService.getFileUrl("/public/images/abc.jpg");

            assertThat(url).isEqualTo("https://" + CF_DOMAIN + "/public/images/abc.jpg");
        }
    }

    // =====================================================================
    // getPresignedUrl
    // =====================================================================
    @Nested
    @DisplayName("getPresignedUrl")
    class GetPresignedUrl {

        @Test
        @DisplayName("실패 - null 키는 FileStorageException(400)을 발생시킨다")
        void 실패_null_키() {
            assertThatThrownBy(() -> s3FileStorageService.getPresignedUrl(null, 60))
                    .isInstanceOf(FileStorageException.class)
                    .satisfies(ex -> assertThat(((FileStorageException) ex).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("성공 - S3Presigner를 통해 Presigned URL을 반환한다")
        void 성공() throws Exception {
            PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
            given(presignedRequest.url()).willReturn(new URL("https://bucket.s3.amazonaws.com/key?X-Amz-Signature=abc"));
            given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).willReturn(presignedRequest);

            String url = s3FileStorageService.getPresignedUrl("public/images/test.jpg", 60);

            assertThat(url).startsWith("https://");
            verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
        }
    }

    // =====================================================================
    // deleteFile
    // =====================================================================
    @Nested
    @DisplayName("deleteFile")
    class DeleteFile {

        @Test
        @DisplayName("예외없음 - null 키는 S3 호출 없이 조용히 무시한다")
        void null_키_S3_호출_안함() {
            s3FileStorageService.deleteFile(null);

            verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("예외없음 - 공백 키는 S3 호출 없이 조용히 무시한다")
        void 빈_키_S3_호출_안함() {
            s3FileStorageService.deleteFile("   ");

            verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("성공 - 정상 키는 S3 삭제 및 CloudFront 캐시 무효화를 호출한다")
        void 성공_S3_삭제_및_CloudFront_무효화_호출() {
            CreateInvalidationResponse cfResponse = mock(CreateInvalidationResponse.class);
            Invalidation invalidation = mock(Invalidation.class);
            given(cfResponse.invalidation()).willReturn(invalidation);
            given(invalidation.id()).willReturn("INV123");
            given(cloudFrontClient.createInvalidation(any(CreateInvalidationRequest.class))).willReturn(cfResponse);

            s3FileStorageService.deleteFile("public/images/test.jpg");

            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
            verify(cloudFrontClient).createInvalidation(any(CreateInvalidationRequest.class));
        }

        @Test
        @DisplayName("성공 - distributionId 미설정 시 CloudFront 무효화를 건너뛴다")
        void 성공_distributionId_미설정_CloudFront_스킵() {
            ReflectionTestUtils.setField(s3FileStorageService, "distributionId", "");

            s3FileStorageService.deleteFile("public/images/test.jpg");

            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
            verify(cloudFrontClient, never()).createInvalidation(any(CreateInvalidationRequest.class));
        }
    }

    // =====================================================================
    // deleteFiles
    // =====================================================================
    @Nested
    @DisplayName("deleteFiles")
    class DeleteFiles {

        @Test
        @DisplayName("빈 결과 - null 목록은 S3 호출 없이 빈 리스트를 반환한다")
        void null_목록_빈_리스트_반환() {
            assertThat(s3FileStorageService.deleteFiles(null)).isEmpty();
            verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
        }

        @Test
        @DisplayName("빈 결과 - 빈 목록은 S3 호출 없이 빈 리스트를 반환한다")
        void 빈_목록_빈_리스트_반환() {
            assertThat(s3FileStorageService.deleteFiles(List.of())).isEmpty();
            verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
        }

        @Test
        @DisplayName("빈 결과 - null/blank 키만 있는 목록은 S3 호출 없이 빈 리스트를 반환한다")
        void null_blank만_있는_목록_빈_리스트_반환() {
            List<String> keys = new ArrayList<>();
            keys.add(null);
            keys.add("   ");

            assertThat(s3FileStorageService.deleteFiles(keys)).isEmpty();
            verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
        }

        @Test
        @DisplayName("성공 - null/blank 키가 섞인 목록에서 유효한 키만 S3에 전송하고 반환한다")
        void 성공_null_blank_섞인_목록_유효_키만_처리() {
            List<String> keys = new ArrayList<>();
            keys.add(null);
            keys.add("   ");
            keys.add("public/images/valid.jpg");

            DeleteObjectsResponse response = DeleteObjectsResponse.builder()
                    .deleted(DeletedObject.builder().key("public/images/valid.jpg").build())
                    .build();
            given(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).willReturn(response);

            List<String> deleted = s3FileStorageService.deleteFiles(keys);

            assertThat(deleted).containsExactly("public/images/valid.jpg");
            verify(s3Client).deleteObjects(any(DeleteObjectsRequest.class));
        }

        @Test
        @DisplayName("성공 - 여러 파일을 일괄 삭제하고 삭제된 키 목록을 반환한다")
        void 성공_여러_파일_일괄_삭제() {
            List<String> keys = List.of("public/images/file1.jpg", "public/images/file2.jpg");

            DeleteObjectsResponse response = DeleteObjectsResponse.builder()
                    .deleted(
                            DeletedObject.builder().key("public/images/file1.jpg").build(),
                            DeletedObject.builder().key("public/images/file2.jpg").build()
                    )
                    .build();
            given(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).willReturn(response);

            List<String> deleted = s3FileStorageService.deleteFiles(keys);

            assertThat(deleted).containsExactlyInAnyOrder("public/images/file1.jpg", "public/images/file2.jpg");
        }
    }
}
