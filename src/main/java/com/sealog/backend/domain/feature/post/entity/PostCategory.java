package com.sealog.backend.domain.feature.post.entity;

import com.sealog.backend.domain.feature.category.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Builder
    public PostCategory(Post post, Category category, Integer sortOrder) {
        this.post = post;
        this.category = category;
        this.sortOrder = sortOrder;
    }
}
