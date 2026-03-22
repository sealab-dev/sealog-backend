package com.sealog.backend.domain.feature.category.integration;

import com.sealog.backend.domain.feature.post.entity.PostCategory;
import com.sealog.backend.domain.feature.post.repository.PostCategoryRepository;
import com.sealog.backend.domain.feature.category.entity.Category;
import com.sealog.backend.domain.feature.category.enums.CategoryGroup;
import com.sealog.backend.domain.feature.category.repository.CategoryRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.support.base.test.IntegrationTest;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Category 통합 테스트 (Controller → Service → Repository)")
public class CategoryIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TestDataFactory testDataFactory;
    @Autowired CategoryRepository categoryRepository;
    @Autowired PostCategoryRepository postCategoryRepository;

    @Nested
    @DisplayName("특정 사용자의 카테고리 목록 조회")
    class GetCategoriesByUser {

        @Test
        @DisplayName("성공 - 사용자가 사용 중인 카테고리를 그룹별로 조회한다")
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
            ResultActions result = mockMvc.perform(get("/api/categories/user/{nickname}", user.getNickname()));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.categories.LANGUAGE[0].name").value(category.getName()));
        }
    }

    @Nested
    @DisplayName("카테고리 자동완성 검색")
    class SearchCategoryByName {

        @Test
        @DisplayName("성공 - 키워드가 포함된 카테고리 목록을 조회한다")
        void success() throws Exception {
            // given
            Category category = testDataFactory.createCategory(CategoryGroup.LANGUAGE);

            // when
            ResultActions result = mockMvc.perform(get("/api/categories/search")
                    .param("keyword", category.getName().substring(0, 2)));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].name").value(category.getName()));
        }

        @Test
        @DisplayName("성공 - 키워드가 없으면 빈 목록을 반환한다")
        void success_empty_keyword() throws Exception {
            // given
            testDataFactory.createCategory(CategoryGroup.LANGUAGE);

            // when
            ResultActions result = mockMvc.perform(get("/api/categories/search")
                    .param("keyword", ""));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}
