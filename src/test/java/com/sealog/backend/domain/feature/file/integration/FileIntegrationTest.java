package com.sealog.backend.domain.feature.file.integration;

import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.support.base.test.IntegrationTest;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * File 통합 테스트 (Controller → Service → Repository)
 */
@DisplayName("File 통합 테스트 (Controller → Service → Repository)")
class FileIntegrationTest extends IntegrationTest {

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
        @DisplayName("성공 - png 파일 업로드 → 200")
        void 성공_png() throws Exception {
            // PNG 매직 바이트 (89 50 4E 47 0D 0A 1A 0A)
            byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.png",
                    "image/png",
                    pngHeader
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .with(user(myDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.originalName").value("test.png"));
        }
    }
}
