package com.sealog.backend.domain.feature.stack.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.entity.PostStack;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostStackRepository;
import com.sealog.backend.domain.feature.stack.dto.StackRequest;
import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.enums.StackGroup;
import com.sealog.backend.domain.feature.stack.repository.StackRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.enums.UserRole;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.support.base.TestIntegrationBase;
import com.sealog.backend.support.component.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Stack 통합 테스트 (Controller → Service → Repository)")
class StackIntegrationTest extends TestIntegrationBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TestDataFactory testDataFactory;
    @Autowired StackRepository stackRepository;
    @Autowired PostStackRepository postStackRepository;

    private User admin;
    private User user;

    @BeforeEach
    void setUp() {
        admin = testDataFactory.createUser(UserRole.ADMIN);
        user = testDataFactory.createUser(UserRole.USER);
    }

    // =========================================================
    // 그룹별 스택 조회
    // =========================================================

    @Nested
    @DisplayName("그룹별 스택 조회 GET /api/guest/stacks/grouped/user/{nickname}")
    class 그룹별_스택_조회 {

        @Test
        @DisplayName("성공 - 스택이 없는 사용자 → 200, 빈 groupedTags 반환")
        void 성공_스택없음() throws Exception {
            mockMvc.perform(get("/api/guest/stacks/grouped/user/{nickname}", user.getNickname()))
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
            mockMvc.perform(get("/api/guest/stacks/grouped/user/{nickname}", user.getNickname()))
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
            mockMvc.perform(get("/api/guest/stacks/grouped/user/{nickname}", user.getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.groupedTags.FRAMEWORK").doesNotExist());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 닉네임 → 404")
        void 존재하지_않는_닉네임() throws Exception {
            mockMvc.perform(get("/api/guest/stacks/grouped/user/{nickname}", "nobody"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================
    // 스택 자동완성
    // =========================================================

    @Nested
    @DisplayName("스택 자동완성 GET /api/guest/stacks/autocomplete")
    class 스택_자동완성 {

        @Test
        @DisplayName("성공 - 매칭되는 키워드 → 200, 스택명 일치 검증")
        void 성공() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);

            mockMvc.perform(get("/api/guest/stacks/autocomplete")
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

            mockMvc.perform(get("/api/guest/stacks/autocomplete")
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

            mockMvc.perform(get("/api/guest/stacks/autocomplete")
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

            mockMvc.perform(get("/api/guest/stacks/autocomplete")
                            .param("keyword", "springboot"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("SpringBoot"));
        }

        @Test
        @DisplayName("성공 - 빈 키워드 → 200, 빈 배열 반환")
        void 빈_키워드() throws Exception {
            mockMvc.perform(get("/api/guest/stacks/autocomplete")
                            .param("keyword", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("성공 - keyword 파라미터 미전달 → 200, 빈 배열 반환")
        void 파라미터_미전달() throws Exception {
            mockMvc.perform(get("/api/guest/stacks/autocomplete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    // =========================================================
    // 스택 목록 조회 (관리자)
    // =========================================================

    @Nested
    @DisplayName("스택 목록 조회 GET /api/admin/stacks")
    class 스택_목록_조회 {

        @Test
        @DisplayName("성공 - ADMIN 권한 요청 → 200, 페이지 응답 반환")
        void 성공() throws Exception {
            CustomUserDetails adminDetails = new CustomUserDetails(admin);

            mockMvc.perform(get("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("성공 - 키워드 검색 → 200, 해당 스택명 포함 확인")
        void 키워드_검색() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.FRAMEWORK);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);

            mockMvc.perform(get("/api/admin/stacks")
                            .param("keyword", "스택")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].name").value(stack.getName()));
        }

        @Test
        @DisplayName("성공 - 매칭 안 되는 키워드 → 200, 빈 content 반환")
        void 매칭_안되는_키워드() throws Exception {
            testDataFactory.createStack(StackGroup.LANGUAGE);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);

            mockMvc.perform(get("/api/admin/stacks")
                            .param("keyword", "없는키워드")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(0));
        }

        @Test
        @DisplayName("성공 - 페이지네이션 파라미터 → size만큼 반환, totalElements 검증")
        void 페이지네이션() throws Exception {
            for (int i = 0; i < 5; i++) {
                testDataFactory.createStack(StackGroup.LANGUAGE);
            }
            CustomUserDetails adminDetails = new CustomUserDetails(admin);

            mockMvc.perform(get("/api/admin/stacks")
                            .param("page", "0")
                            .param("size", "3")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(3))
                    .andExpect(jsonPath("$.data.totalElements").value(5));
        }

        @Test
        @DisplayName("실패 - USER 권한 요청 → 403")
        void USER_권한() throws Exception {
            CustomUserDetails normalDetails = new CustomUserDetails(user);

            mockMvc.perform(get("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(normalDetails)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청 → 401")
        void 인증_없음() throws Exception {
            mockMvc.perform(get("/api/admin/stacks"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================
    // 스택 생성 (관리자)
    // =========================================================

    @Nested
    @DisplayName("스택 생성 POST /api/admin/stacks")
    class 스택_생성 {

        @Test
        @DisplayName("성공 - 유효한 요청 → 201, 생성된 스택 반환 (ID 존재 검증)")
        void 성공() throws Exception {
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Create request = StackRequest.Create.builder()
                    .name("Spring Boot")
                    .stackGroup("framework")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.name").value("Spring Boot"))
                    .andExpect(jsonPath("$.data.stackGroup").value("FRAMEWORK"))
                    .andExpect(jsonPath("$.message").value("스택이 생성되었습니다"));
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 스택명 → 409")
        void 스택명_중복() throws Exception {
            Stack existingStack = testDataFactory.createStack(StackGroup.LANGUAGE);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Create request = StackRequest.Create.builder()
                    .name(existingStack.getName())
                    .stackGroup("language")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패 - 스택명 없이 요청 → 400")
        void 스택명_없음() throws Exception {
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Create request = StackRequest.Create.builder()
                    .stackGroup("language")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 스택명이 공백 문자열 → 400")
        void 스택명_공백() throws Exception {
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Create request = StackRequest.Create.builder()
                    .name("   ")
                    .stackGroup("language")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 스택명 50자 초과 → 400")
        void 스택명_길이_초과() throws Exception {
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Create request = StackRequest.Create.builder()
                    .name("A".repeat(51))
                    .stackGroup("language")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - stackGroup 없이 요청 → 400")
        void 스택그룹_없음() throws Exception {
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Create request = StackRequest.Create.builder()
                    .name("새스택")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 stackGroup → 400")
        void 유효하지_않은_스택그룹() throws Exception {
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Create request = StackRequest.Create.builder()
                    .name("새스택")
                    .stackGroup("invalid_group")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - USER 권한 요청 → 403")
        void USER_권한() throws Exception {
            CustomUserDetails userDetails = new CustomUserDetails(user);
            StackRequest.Create request = StackRequest.Create.builder()
                    .name("새스택")
                    .stackGroup("language")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청 → 401")
        void 인증_없음() throws Exception {
            StackRequest.Create request = StackRequest.Create.builder()
                    .name("새스택")
                    .stackGroup("language")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================
    // 스택 수정 (관리자)
    // =========================================================

    @Nested
    @DisplayName("스택 수정 PUT /api/admin/stacks/{stackId}")
    class 스택_수정 {

        @Test
        @DisplayName("성공 - 유효한 요청 → 200, 수정된 스택 반환 (ID 일치 검증)")
        void 성공() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Update request = StackRequest.Update.builder()
                    .name("수정된스택명")
                    .stackGroup("framework")
                    .build();

            mockMvc.perform(put("/api/admin/stacks/{stackId}", stack.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(stack.getId()))
                    .andExpect(jsonPath("$.data.name").value("수정된스택명"))
                    .andExpect(jsonPath("$.data.stackGroup").value("FRAMEWORK"))
                    .andExpect(jsonPath("$.message").value("스택이 수정되었습니다"));
        }

        @Test
        @DisplayName("성공 - 동일한 이름으로 수정 → 200")
        void 동일한_이름으로_수정() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Update request = StackRequest.Update.builder()
                    .name(stack.getName())
                    .stackGroup("devops")
                    .build();

            mockMvc.perform(put("/api/admin/stacks/{stackId}", stack.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value(stack.getName()));
        }

        @Test
        @DisplayName("실패 - 다른 스택이 사용 중인 이름으로 수정 → 409")
        void 스택명_중복() throws Exception {
            Stack stack1 = testDataFactory.createStack(StackGroup.LANGUAGE);
            Stack stack2 = testDataFactory.createStack(StackGroup.FRAMEWORK);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Update request = StackRequest.Update.builder()
                    .name(stack1.getName())
                    .stackGroup("framework")
                    .build();

            mockMvc.perform(put("/api/admin/stacks/{stackId}", stack2.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 스택 → 404")
        void 존재하지_않는_스택() throws Exception {
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Update request = StackRequest.Update.builder()
                    .name("수정된스택명")
                    .stackGroup("language")
                    .build();

            mockMvc.perform(put("/api/admin/stacks/{stackId}", 9999L)
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 스택명이 공백 문자열 → 400")
        void 스택명_공백() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Update request = StackRequest.Update.builder()
                    .name("   ")
                    .stackGroup("language")
                    .build();

            mockMvc.perform(put("/api/admin/stacks/{stackId}", stack.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - stackGroup 없이 요청 → 400")
        void 스택그룹_없음() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);
            StackRequest.Update request = StackRequest.Update.builder()
                    .name("수정된스택명")
                    .build();

            mockMvc.perform(put("/api/admin/stacks/{stackId}", stack.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - USER 권한 요청 → 403")
        void USER_권한() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);
            CustomUserDetails userDetails = new CustomUserDetails(user);
            StackRequest.Update request = StackRequest.Update.builder()
                    .name("수정된스택명")
                    .stackGroup("language")
                    .build();

            mockMvc.perform(put("/api/admin/stacks/{stackId}", stack.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청 → 401")
        void 인증_없음() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);
            StackRequest.Update request = StackRequest.Update.builder()
                    .name("수정된스택명")
                    .stackGroup("language")
                    .build();

            mockMvc.perform(put("/api/admin/stacks/{stackId}", stack.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================
    // 스택 삭제 (관리자)
    // =========================================================

    @Nested
    @DisplayName("스택 삭제 DELETE /api/admin/stacks/{stackId}")
    class 스택_삭제 {

        @Test
        @DisplayName("성공 - 존재하는 스택 삭제 → 204")
        void 성공() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.TOOL);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);

            mockMvc.perform(delete("/api/admin/stacks/{stackId}", stack.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("성공 - 게시글에 연결된 스택 삭제 → 204, PostStack도 함께 삭제")
        void 성공_연결된_PostStack_함께_삭제() throws Exception {
            // given
            Stack stack = testDataFactory.createStack(StackGroup.TOOL);
            Post post = testDataFactory.createPost(user, PostStatus.PUBLISHED);
            PostStack postStack = postStackRepository.save(PostStack.builder()
                    .post(post)
                    .stack(stack)
                    .sortOrder(0)
                    .build());
            CustomUserDetails adminDetails = new CustomUserDetails(admin);

            // when
            mockMvc.perform(delete("/api/admin/stacks/{stackId}", stack.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails)))
                    .andExpect(status().isNoContent());

            // then
            assertThat(postStackRepository.existsById(postStack.getId())).isFalse();
            assertThat(stackRepository.existsById(stack.getId())).isFalse();
        }

        @Test
        @DisplayName("성공 - 삭제 후 동일 이름으로 재생성 → 201")
        void 삭제_후_재생성() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.LANGUAGE);
            CustomUserDetails adminDetails = new CustomUserDetails(admin);

            // 삭제
            mockMvc.perform(delete("/api/admin/stacks/{stackId}", stack.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails)))
                    .andExpect(status().isNoContent());

            // 동일 이름으로 재생성
            StackRequest.Create request = StackRequest.Create.builder()
                    .name(stack.getName())
                    .stackGroup("language")
                    .build();

            mockMvc.perform(post("/api/admin/stacks")
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value(stack.getName()));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 스택 → 404")
        void 존재하지_않는_스택() throws Exception {
            CustomUserDetails adminDetails = new CustomUserDetails(admin);

            mockMvc.perform(delete("/api/admin/stacks/{stackId}", 9999L)
                            .with(SecurityMockMvcRequestPostProcessors.user(adminDetails)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - USER 권한 요청 → 403")
        void USER_권한() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.TOOL);
            CustomUserDetails normalDetails = new CustomUserDetails(user);

            mockMvc.perform(delete("/api/admin/stacks/{stackId}", stack.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(normalDetails)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청 → 401")
        void 인증_없음() throws Exception {
            Stack stack = testDataFactory.createStack(StackGroup.TOOL);

            mockMvc.perform(delete("/api/admin/stacks/{stackId}", stack.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }
}
