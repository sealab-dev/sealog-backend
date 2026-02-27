package com.sealog.backend.domain.feature.collections.entity;

import com.sealog.backend.domain.feature.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "collection_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collections collection;

    /**
     * 컬렉션 아이템 연결 대상 (post_id)
     * - null 허용: item_type에 따라 다른 엔티티와 연결될 수 있음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    public CollectionItem(Collections collection, Post post, int sortOrder) {
        this.collection = collection;
        this.post = post;
        this.sortOrder = sortOrder;
    }

    // === 비즈니스 로직 === //
    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
