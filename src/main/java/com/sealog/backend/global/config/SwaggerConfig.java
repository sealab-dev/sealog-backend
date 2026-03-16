package com.sealog.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 문서 설정
 *
 * 그룹 구분 기준:
 *  - admin  : /api/admin/**   → ADMIN 권한 필요
 *  - user   : /api/me/**,     → 로그인 사용자 전용
 *             /api/files/**
 *  - auth   : /api/auth/**    → 인증 관련 (로그인·재발급은 공개, /me·logout은 인증 필요)
 *  - public : 그 외 /api/**  → 누구나 접근 가능
 *
 * 모든 응답은 ResponseWrapperAdvice에 의해 아래 구조로 래핑됩니다.
 * {
 *   "success" : true,
 *   "data"    : { ... },   // 각 API의 실제 응답 데이터
 *   "message" : "...",     // 액션 성공 메시지 (선택)
 *   "status"  : 200        // HTTP 상태 코드
 * }
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sealog API")
                        .version("v1")
                        .description("""
                                Sealog 백엔드 API 문서입니다.

                                **공통 응답 구조** (모든 응답은 아래 봉투에 래핑됩니다)
                                ```json
                                {
                                  "success": true,
                                  "data":    { },
                                  "message": "액션 성공 메시지 (선택)",
                                  "status":  200
                                }
                                ```

                                **인증 방식**: JWT Bearer Token (HttpOnly 쿠키)
                                """))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }

    /** ADMIN 권한 필요 API: /api/admin/** */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("Admin API (ADMIN 권한 필요)")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    /** 로그인 사용자 전용 API: /api/me/**, /api/files/** */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .displayName("User API (로그인 필요)")
                .pathsToMatch("/api/me/**", "/api/files/**")
                .build();
    }

    /** 인증 관련 API: /api/auth/** */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("Auth API (인증)")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /** 공개 API: /api/admin, /api/me, /api/files, /api/auth 를 제외한 나머지 */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Public API (인증 불필요)")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/admin/**", "/api/me/**", "/api/files/**", "/api/auth/**")
                .build();
    }
}
