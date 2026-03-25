package com.sealog.backend.domain.feature.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sealog.backend.domain.feature.post.dto.PostMeRequest;
import com.sealog.backend.domain.feature.post.service.PostService;
import com.sealog.backend.global.exception.GlobalExceptionHandler;
import com.sealog.backend.security.auth.CustomUserDetails;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PostMeController 단위 테스트 - 유효성 검증")
class PostMeControllerUnitTest extends UnitTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostMeController postMeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postMeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("게시글 생성 POST /api/me/posts - 유효성 검증 실패")
    class CreatePost {

        @Test
        @DisplayName("실패 - 제목 공백 → 400")
        void 제목_공백() throws Exception {
            PostMeRequest.Create request = PostMeRequest.Create.builder()
                    .title("")
                    .content("내용")
                    .build();

            mockMvc.perform(multipart(HttpMethod.POST, "/api/me/posts")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("제목을 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 제목 50자 초과 → 400")
        void 제목_너무_김() throws Exception {
            PostMeRequest.Create request = PostMeRequest.Create.builder()
                    .title("a".repeat(51))
                    .content("내용")
                    .build();

            mockMvc.perform(multipart(HttpMethod.POST, "/api/me/posts")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("제목은 50자 이내로 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 내용 공백 → 400")
        void 내용_공백() throws Exception {
            PostMeRequest.Create request = PostMeRequest.Create.builder()
                    .title("제목")
                    .content("")
                    .build();

            mockMvc.perform(multipart(HttpMethod.POST, "/api/me/posts")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("내용을 입력해주세요"));
        }

        @Test
        @DisplayName("실패 - 태그 5개 초과 → 400")
        void 태그_너무_많음() throws Exception {
            List<String> tags = IntStream.range(0, 6).mapToObj(i -> "tag" + i).collect(Collectors.toList());
            PostMeRequest.Create request = PostMeRequest.Create.builder()
                    .title("제목")
                    .content("내용")
                    .tags(tags)
                    .build();

            mockMvc.perform(multipart(HttpMethod.POST, "/api/me/posts")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("태그는 최대 5개까지 등록할 수 있습니다"));
        }

        @Test
        @DisplayName("실패 - 카테고리 5개 초과 → 400")
        void 카테고리_너무_많음() throws Exception {
            List<Long> categoryIds = IntStream.range(0, 6).mapToObj(i -> (long) i).collect(Collectors.toList());
            PostMeRequest.Create request = PostMeRequest.Create.builder()
                    .title("제목")
                    .content("내용")
                    .categoryIds(categoryIds)
                    .build();

            mockMvc.perform(multipart(HttpMethod.POST, "/api/me/posts")
                            .file(toMultipartJson("request", request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("카테고리는 최대 5개까지 등록할 수 있습니다"));
        }
    }

    private MockMultipartFile toMultipartJson(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name, "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value)
        );
    }
}
