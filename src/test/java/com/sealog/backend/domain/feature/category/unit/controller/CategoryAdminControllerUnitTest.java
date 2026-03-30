package com.sealog.backend.domain.feature.category.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.category.controller.CategoryAdminController;
import com.sealog.backend.domain.feature.category.service.CategoryService;
import com.sealog.backend.global.exception.GlobalExceptionHandler;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CategoryAdminController 단위 테스트 - 유효성 검증")
class CategoryAdminControllerUnitTest extends UnitTest {

    @Mock CategoryService categoryService;
    @InjectMocks
    CategoryAdminController categoryAdminController;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryAdminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // =====================================================================
    // 카테고리 생성
    // =====================================================================
    @Nested
    @DisplayName("카테고리 생성 POST /api/admin/categories - 유효성 검증 실패")
    class Create {

        @Test
        @DisplayName("실패 - 카테고리명 공백 → 400")
        void 카테고리명_공백() throws Exception {
            mockMvc.perform(post("/api/admin/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "", "categoryGroup", "framework"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("카테고리명은 필수입니다."));
        }

        @Test
        @DisplayName("실패 - 카테고리명 50자 초과 → 400")
        void 카테고리명_너무_김() throws Exception {
            mockMvc.perform(post("/api/admin/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "a".repeat(51), "categoryGroup", "framework"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("카테고리명은 최대 50자입니다."));
        }

        @Test
        @DisplayName("실패 - 카테고리 그룹 공백 → 400")
        void 카테고리_그룹_공백() throws Exception {
            mockMvc.perform(post("/api/admin/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "Spring Boot", "categoryGroup", ""))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("카테고리 그룹은 필수입니다."));
        }
    }

    // =====================================================================
    // 카테고리 수정
    // =====================================================================
    @Nested
    @DisplayName("카테고리 수정 PUT /api/admin/categories/{categoryId} - 유효성 검증 실패")
    class Update {

        @Test
        @DisplayName("실패 - 카테고리명 공백 → 400")
        void 카테고리명_공백() throws Exception {
            mockMvc.perform(put("/api/admin/categories/{categoryId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "", "categoryGroup", "framework"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("카테고리명은 필수입니다."));
        }

        @Test
        @DisplayName("실패 - 카테고리명 50자 초과 → 400")
        void 카테고리명_너무_김() throws Exception {
            mockMvc.perform(put("/api/admin/categories/{categoryId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "a".repeat(51), "categoryGroup", "framework"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("카테고리명은 최대 50자입니다."));
        }
    }
}
