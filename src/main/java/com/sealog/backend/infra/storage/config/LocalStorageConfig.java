package com.sealog.backend.infra.storage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 로컬 파일 저장소 설정
 * /files/** 경로로 로컬 파일 접근 가능하도록 ResourceHandler 등록
 */
@Configuration
@Profile({"local", "test"})
@RequiredArgsConstructor
public class LocalStorageConfig implements WebMvcConfigurer {

    // 사용 상수
    @Value("${file.local.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }
}
