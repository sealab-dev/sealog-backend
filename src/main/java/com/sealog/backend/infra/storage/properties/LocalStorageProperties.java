package com.sealog.backend.infra.storage.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Profile({"local", "test"})
@ConfigurationProperties(prefix = "file.local")
public class LocalStorageProperties {

    private String uploadDir = "./uploads";
    private String baseUrl = "http://localhost:8080/files/";
}
