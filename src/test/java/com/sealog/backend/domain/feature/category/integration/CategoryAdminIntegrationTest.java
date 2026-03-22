package com.sealog.backend.domain.feature.category.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.category.dto.CategoryAdminRequest;
import com.sealog.backend.domain.feature.category.entity.Category;
import com.sealog.backend.domain.feature.category.enums.CategoryGroup;
import com.sealog.backend.domain.feature.category.repository.CategoryRepository;
import com.sealog.backend.domain.feature.post.entity.PostCategory;
import com.sealog.backend.domain.feature.post.repository.PostCategoryRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.support.base.test.IntegrationTest;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Category Admin 통합 테스트 (관리자 기능)")
public class CategoryAdminIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;
    @Autowired CategoryRepository categoryRepository;
    @Autowired PostCategoryRepository postCategoryRepository;

    @Nested
    @DisplayName("전체 카테고리 페이징 조회")
    class GetAll {

        @Test
        @DisplayName("성공 - 전체 카테고리 목록을 페이징하여 조회한다")
        void success() throws Exception {
            // given
            Category category = testDataFactory.createCategory(CategoryGroup.FRAMEWORK);

            // when
            ResultActions result = mockMvc.perform(get("/api/admin/categories")
                            .with(user("admin").roles("ADMIN")))
                    .andDo(print());

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].name").value(category.getName()));
        }

        @Test
        @DisplayName("성공 - 키워드로 필터링하여 조회한다")
        void success_with_keyword() throws Exception {
            // given
            testDataFactory.createCategory(CategoryGroup.LANGUAGE);
            Category category = testDataFactory.createCategory(CategoryGroup.FRAMEWORK);

            // when
            ResultActions result = mockMvc.perform(get("/api/admin/categories")
                            .param("keyword", category.getName())
                            .with(user("admin").roles("ADMIN")));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].name").value(category.getName()));


        }
    }

    @Nested
    @DisplayName("새 카테고리 생성")
    class Create {

        @Test
        @DisplayName("성공 - 새로운 카테고리를 생성한다")
        void success() throws Exception {
            // given
            CategoryAdminRequest.Create request = new CategoryAdminRequest.Create();
            setField(request, "name", "New Category");
            setField(request, "categoryGroup", "FRAMEWORK");

            // when
            ResultActions result = mockMvc.perform(post("/api/admin/categories")
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("New Category"))
                    .andExpect(jsonPath("$.data.categoryGroup").value("FRAMEWORK"));
        }
    }

    @Nested
    @DisplayName("카테고리 정보 수정")
    class Update {

        @Test
        @DisplayName("성공 - 카테고리 이름과 그룹을 수정한다")
        void success() throws Exception {
            // given
            Category category = testDataFactory.createCategory(CategoryGroup.LANGUAGE);
            CategoryAdminRequest.Update request = new CategoryAdminRequest.Update();
            setField(request, "name", "Updated Name");
            setField(request, "categoryGroup", "FRAMEWORK");

            // when
            ResultActions result = mockMvc.perform(put("/api/admin/categories/{categoryId}", category.getId())
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(category.getId()))
                    .andExpect(jsonPath("$.data.name").value("Updated Name"))
                    .andExpect(jsonPath("$.data.categoryGroup").value("FRAMEWORK"));
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class Delete {

        @Test
        @DisplayName("성공 - 카테고리를 삭제하고 연결된 매핑 정보도 삭제한다")
        void success() throws Exception {
            // given
            User user = testDataFactory.createUser();
            Category category = testDataFactory.createCategory(CategoryGroup.LANGUAGE);
            var post = testDataFactory.createPost(user);

            postCategoryRepository.save(PostCategory.builder()
                    .post(post)
                    .category(category)
                    .sortOrder(0)
                    .build());

            // when
            ResultActions result = mockMvc.perform(delete("/api/admin/categories/{categoryId}", category.getId())
                    .with(user("admin").roles("ADMIN")));

            // then
            result.andExpect(status().isOk());
            assertThat(categoryRepository.findById(category.getId())).isEmpty();
            assertThat(postCategoryRepository.findAllByPostIdOrderBySortOrderAsc(post.getId())).isEmpty();
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
