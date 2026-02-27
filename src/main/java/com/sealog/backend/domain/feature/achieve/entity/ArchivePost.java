package com.sealog.backend.domain.feature.achieve.entity;

import com.sealog.backend.domain.feature.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "archive_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchivePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id", nullable = false)
    private Archive archive;

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
    public ArchivePost(Archive archive, Post post, int sortOrder) {
        this.archive = archive;
        this.post = post;
        this.sortOrder = sortOrder;
    }

    // === 비즈니스 로직 === //
    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
