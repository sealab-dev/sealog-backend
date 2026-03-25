package com.sealog.backend.domain.feature.post.util;

import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PostSlugGenerator 단위 테스트")
class PostSlugGeneratorUnitTest extends UnitTest {

    @Test
    @DisplayName("슬러그 생성: 제목을 URL-safe한 슬러그로 변환한다.")
    void generate_Success() {
        assertThat(PostSlugGenerator.generate("Spring Boot 시작하기"))
                .isEqualTo("spring-boot-시작하기");
        
        assertThat(PostSlugGenerator.generate("React 입문 가이드!!!"))
                .isEqualTo("react-입문-가이드");
        
        assertThat(PostSlugGenerator.generate("Java의 Stream API & Lambda"))
                .isEqualTo("java의-stream-api-lambda");
    }

    @Test
    @DisplayName("중복 슬러그 처리: 기본 슬러그에 접미사 번호를 추가한다.")
    void generateWithSuffix_Success() {
        String base = "test-slug";
        assertThat(PostSlugGenerator.generateWithSuffix(base, 2)).isEqualTo("test-slug-2");
        assertThat(PostSlugGenerator.generateWithSuffix(base, 10)).isEqualTo("test-slug-10");
    }

    @Test
    @DisplayName("유효성 검증: 슬러그 형식이 올바른지 확인한다.")
    void isValid_Success() {
        assertThat(PostSlugGenerator.isValid("valid-slug-123")).isTrue();
        assertThat(PostSlugGenerator.isValid("한글-슬러그")).isTrue();
        assertThat(PostSlugGenerator.isValid("Invalid Slug")).isFalse();
        assertThat(PostSlugGenerator.isValid("special!chars")).isFalse();
    }

    @Test
    @DisplayName("실패: 빈 제목으로는 슬러그를 생성할 수 없다.")
    void generate_Fail_Empty() {
        assertThatThrownBy(() -> PostSlugGenerator.generate(""))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> PostSlugGenerator.generate("!!!"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
