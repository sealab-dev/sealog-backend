package com.sealog.backend.domain.feature.stack.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.entity.PostStack;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostStackRepository;
import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.enums.StackGroup;
import com.sealog.backend.domain.feature.stack.repository.StackRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Stack 통합 테스트 (Controller → Service → Repository)")
class StackIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;
    @Autowired StackRepository stackRepository;
    @Autowired PostStackRepository postStackRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = testDataFactory.createUser(UserRole.USER);
    }

    // =========================================================
    // 그룹별 스택 조회
    // =========================================================

    @Nested
    @DisplayName("그룹별 스택 조회 GET /api/stacks/user/{nickname}")
    class 그룹별_스택_조회 {

        @Test
        @DisplayName("성공 - 스택이 없는 사용자 → 200, 빈 groupedTags 반환")
        void 성공_스택없음() throws Exception {
            mockMvc.perform(get("/api/stacks/user/{nickname}", user.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.groupedTags").exists());
        }

        @Test
        @DisplayName("성공 - 공개 게시글에 연결된 스택 → 그룹, 스택명, postCount 검증")
        void 성공_스택_데이터_검증() throws Exception {
            // given
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);
            Post post = testDataFactory.createPost(user, PostStatus.PUBLISHED);
            postStackRepository.save(PostStack.builder()
                    .post(post)
                    .stack(stack)
                    .sortOrder(0)
                    .build());

            // when & then
            mockMvc.perform(get("/api/stacks/user/{nickname}", user.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.groupedTags.LANGUAGE").isArray())
                    .andExpect(jsonPath("$.data.groupedTags.LANGUAGE[0].name").value(stack.getName()))
                    .andExpect(jsonPath("$.data.groupedTags.LANGUAGE[0].postCount").value(1));
        }

        @Test
        @DisplayName("성공 - 비공개(DRAFT) 게시글 스택은 집계에서 제외")
        void 성공_비공개_게시글_제외() throws Exception {
            // given
            Stack stack = testDataFactory.createStack(StackGroup.FRAMEWORK);
            Post draftPost = testDataFactory.createPost(user, PostStatus.DRAFT);
            postStackRepository.save(PostStack.builder()
                    .post(draftPost)
                    .stack(stack)
                    .sortOrder(0)
                    .build());

            // when & then
            mockMvc.perform(get("/api/stacks/user/{nickname}", user.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.groupedTags.FRAMEWORK").doesNotExist());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 닉네임 → 404")
        void 존재하지_않는_닉네임() throws Exception {
            mockMvc.perform(get("/api/stacks/user/{nickname}", "nobody"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // =========================================================
    // 스택 자동완성
    // =========================================================

    @Nested
    @DisplayName("스택 자동완성 GET /api/stacks/search")
    class 스택_자동완성 {

        @Test
        @DisplayName("성공 - 매칭되는 키워드 → 200, 스택명 일치 검증")
        void 성공() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);

            mockMvc.perform(get("/api/stacks/search")
                            .param("keyword", "스택"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value(stack.getName()));
        }

        @Test
        @DisplayName("성공 - 매칭 안 되는 키워드 → 200, 빈 배열 반환")
        void 매칭_안되는_키워드() throws Exception {
            testDataFactory.createStack(StackGroup.LANGUAGE);

            mockMvc.perform(get("/api/stacks/search")
                            .param("keyword", "없는키워드"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("성공 - 6개 생성 시 5개만 반환 (자동완성 최대 5개 제한)")
        void 최대_5개_제한() throws Exception {
            for (int i = 0; i < 6; i++) {
                testDataFactory.createStack(StackGroup.LANGUAGE);
            }

            mockMvc.perform(get("/api/stacks/search")
                            .param("keyword", "스택"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(5));
        }

        @Test
        @DisplayName("성공 - 대소문자 무시 검색")
        void 대소문자_무시() throws Exception {
            stackRepository.save(Stack.builder()
                    .name("SpringBoot")
                    .stackGroup(StackGroup.FRAMEWORK)
                    .build());

            mockMvc.perform(get("/api/stacks/search")
                            .param("keyword", "springboot"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("SpringBoot"));
        }

        @Test
        @DisplayName("성공 - 빈 키워드 → 200, 빈 배열 반환")
        void 빈_키워드() throws Exception {
            mockMvc.perform(get("/api/stacks/search")
                            .param("keyword", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("성공 - keyword 파라미터 미전달 → 200, 빈 배열 반환")
        void 파라미터_미전달() throws Exception {
            mockMvc.perform(get("/api/stacks/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }
}
