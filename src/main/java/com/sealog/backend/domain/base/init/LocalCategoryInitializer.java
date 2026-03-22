package com.sealog.backend.domain.base.init;

import com.sealog.backend.domain.feature.category.entity.Category;
import com.sealog.backend.domain.feature.category.enums.CategoryGroup;
import com.sealog.backend.domain.feature.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class LocalCategoryInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        saveCategory("Java", CategoryGroup.LANGUAGE);
        saveCategory("Spring Boot", CategoryGroup.FRAMEWORK);
        saveCategory("React", CategoryGroup.FRAMEWORK);
        saveCategory("MySQL", CategoryGroup.DATABASE);
        saveCategory("Redis", CategoryGroup.DATABASE);
        saveCategory("Docker", CategoryGroup.DEVOPS);
        saveCategory("AWS", CategoryGroup.DEVOPS);
        saveCategory("GitHub Actions", CategoryGroup.DEVOPS);
    }

    private void saveCategory(String name, CategoryGroup group) {
        if (categoryRepository.findByName(name).isEmpty()) {
            Category category = Category.builder()
                    .name(name)
                    .categoryGroup(group)
                    .build();
            categoryRepository.save(category);
        }
    }
}
