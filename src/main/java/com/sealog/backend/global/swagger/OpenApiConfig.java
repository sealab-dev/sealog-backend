package com.sealog.backend.global.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sealog API")
                        .version("v1")
                        .description("API documentation"))
                // 전체 API에 기본으로 JWT 보안 요구사항 적용
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

    // 관리자 API 문서
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }


    // 일반 API 문서
    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/me/**","/api/my/**","/api/files/**")
                .pathsToExclude("/api/admin/**","/api/auth/**","/api/user/**","/api/stack/**")
                .build();
    }



    @Bean
    public GroupedOpenApi guestApi() {
        return GroupedOpenApi.builder()
                .group("guest")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/admin/**","/api/me/**","/api/my/**","/api/files/**")
                .build();
    }
}