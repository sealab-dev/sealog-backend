package com.sealog.backend.infra.storage.config;

import com.sealog.backend.infra.storage.properties.LocalStorageProperties;
import lombok.RequiredArgsConstructor;
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
@Profile("local")
@RequiredArgsConstructor
public class LocalStorageConfig implements WebMvcConfigurer {

    private final LocalStorageProperties localStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(localStorageProperties.getUploadDir()).toAbsolutePath().normalize();

        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }
}
