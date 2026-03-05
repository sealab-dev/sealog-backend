package com.sealog.backend.domain.feature.post.entity;

import com.sealog.backend.domain.feature.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Builder
    public PostTag(Post post, Tag tag, Integer sortOrder) {
        this.post = post;
        this.tag = tag;
        this.sortOrder = sortOrder;
    }
}
