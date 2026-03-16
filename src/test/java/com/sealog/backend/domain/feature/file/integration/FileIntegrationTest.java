package com.sealog.backend.domain.feature.file.integration;

import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * File 통합 테스트 (Controller → Service → Repository)
 */
@DisplayName("File 통합 테스트 (Controller → Service → Repository)")
class FileIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired TestDataFactory testDataFactory;

    private User testUser;
    private CustomUserDetails myDetails;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        testUser  = testDataFactory.createUser(UserRole.USER);

        // Mock 인증 컨텍스트 생성 (JWT 필터 우회)
        myDetails = new CustomUserDetails(testUser);
    }

    @Test
    @Order(0)
    void warmUp() {
        // 아무것도 안 함, JVM 웜업용
    }

    // =====================================================================
    // 파일 업로드
    // =====================================================================
    @Nested
    @DisplayName("파일 업로드 (POST /api/files/upload)")
    class 파일_업로드 {

        @Test
        @DisplayName("성공 - 이미지 파일 업로드 → 200")
        void 성공_이미지() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.originalName").value("test.jpg"));
        }

        @Test
        @DisplayName("성공 - 문서 파일 업로드 → 200")
        void 성공_문서() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "%PDF-1.4".getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.originalName").value("document.pdf"));
        }

        @Test
        @DisplayName("실패 - 미인증 → 401")
        void 미인증() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "fake image content".getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .with(anonymous()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 빈 파일 → 400")
        void 빈_파일() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "empty.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .with(user(myDetails)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 허용되지 않는 확장자 → 400")
        void 허용되지_않는_확장자() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "malware.exe",
                    "application/octet-stream",
                    "fake content".getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .with(user(myDetails)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 경로 조작 파일명 → 400")
        void 경로_조작_파일명() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "../etc/passwd",
                    "image/jpeg",
                    "fake content".getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .with(user(myDetails)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
