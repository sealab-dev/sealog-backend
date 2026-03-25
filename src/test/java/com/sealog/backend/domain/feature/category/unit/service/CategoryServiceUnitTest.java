package com.sealog.backend.domain.feature.category.unit.service;

import com.sealog.backend.domain.feature.category.dto.CategoryAdminRequest;
import com.sealog.backend.domain.feature.category.entity.Category;
import com.sealog.backend.domain.feature.category.enums.CategoryGroup;
import com.sealog.backend.domain.feature.category.repository.CategoryRepository;
import com.sealog.backend.domain.feature.category.service.CategoryServiceImpl;
import com.sealog.backend.domain.feature.post.repository.PostCategoryRepository;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("CategoryService 단위 테스트")
class CategoryServiceUnitTest extends UnitTest {

    @Mock CategoryRepository categoryRepository;
    @Mock PostCategoryRepository postCategoryRepository;

    @InjectMocks
    CategoryServiceImpl categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .name("Spring Boot")
                .categoryGroup(CategoryGroup.FRAMEWORK)
                .build();
        ReflectionTestUtils.setField(testCategory, "id", 1L);
    }

    // =====================================================================
    // 카테고리 생성
    // =====================================================================
    @Nested
    @DisplayName("카테고리 생성")
    class CreateCategory {

        @Test
        @DisplayName("실패 - 이미 존재하는 카테고리명 → 400 예외")
        void 이름_중복() {
            given(categoryRepository.existsByName("Spring Boot")).willReturn(true);

            CategoryAdminRequest.Create request = buildCreateRequest("Spring Boot", "framework");

            assertThatThrownBy(() -> categoryService.createCategory(request, 1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ce.getMessage()).contains("이미 존재하는 카테고리 이름입니다");
                    });

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 카테고리 그룹 키 → 400 예외")
        void 잘못된_그룹_키() {
            given(categoryRepository.existsByName("Spring Boot")).willReturn(false);

            CategoryAdminRequest.Create request = buildCreateRequest("Spring Boot", "INVALID_KEY");

            assertThatThrownBy(() -> categoryService.createCategory(request, 1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ce.getMessage()).contains("유효하지 않은 카테고리 그룹입니다");
                    });

            verify(categoryRepository, never()).save(any());
        }
    }

    // =====================================================================
    // 카테고리 수정
    // =====================================================================
    @Nested
    @DisplayName("카테고리 수정")
    class UpdateCategory {

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리 ID → 404 예외")
        void 카테고리_없음() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            CategoryAdminRequest.Update request = buildUpdateRequest("Spring Boot 3.0", "framework");

            assertThatThrownBy(() -> categoryService.updateCategory(99L, request, 1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 변경할 이름이 이미 다른 카테고리에서 사용 중 → 400 예외")
        void 이름_중복() {
            given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));
            given(categoryRepository.existsByName("React")).willReturn(true);

            CategoryAdminRequest.Update request = buildUpdateRequest("React", "framework");

            assertThatThrownBy(() -> categoryService.updateCategory(1L, request, 1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ce.getMessage()).contains("이미 존재하는 카테고리 이름입니다");
                    });
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 카테고리 그룹 키 → 400 예외")
        void 잘못된_그룹_키() {
            given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

            CategoryAdminRequest.Update request = buildUpdateRequest("Spring Boot", "INVALID_KEY");

            assertThatThrownBy(() -> categoryService.updateCategory(1L, request, 1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ce.getMessage()).contains("유효하지 않은 카테고리 그룹입니다");
                    });
        }
    }

    // =====================================================================
    // 카테고리 삭제
    // =====================================================================
    @Nested
    @DisplayName("카테고리 삭제")
    class DeleteCategory {

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리 ID → 404 예외")
        void 카테고리_없음() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(99L, 1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));

            verify(postCategoryRepository, never()).deleteAllByCategoryId(any());
            verify(categoryRepository, never()).delete(any());
        }
    }

    // =====================================================================
    // 헬퍼 메서드
    // =====================================================================

    private CategoryAdminRequest.Create buildCreateRequest(String name, String categoryGroup) {
        CategoryAdminRequest.Create request = new CategoryAdminRequest.Create();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "categoryGroup", categoryGroup);
        return request;
    }

    private CategoryAdminRequest.Update buildUpdateRequest(String name, String categoryGroup) {
        CategoryAdminRequest.Update request = new CategoryAdminRequest.Update();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "categoryGroup", categoryGroup);
        return request;
    }
}
