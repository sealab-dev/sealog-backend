package com.sealog.backend.domain.feature.category.entity;

import com.sealog.backend.domain.base.entity.BaseTimeEntity;
import com.sealog.backend.domain.feature.category.enums.CategoryGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryGroup categoryGroup;

    @Builder
    public Category(String name, CategoryGroup categoryGroup) {
        this.name = name;
        this.categoryGroup = categoryGroup != null ? categoryGroup : CategoryGroup.ETC;
    }

    /**
     * 카테고리명 변경
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 카테고리 그룹 변경
     */
    public void updateCategoryGroup(CategoryGroup categoryGroup) {
        this.categoryGroup = categoryGroup;
    }
}
