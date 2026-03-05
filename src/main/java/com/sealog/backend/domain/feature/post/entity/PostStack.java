package com.sealog.backend.domain.feature.post.entity;

import com.sealog.backend.domain.feature.stack.entity.Stack;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_stack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stack_id", nullable = false)
    private Stack stack;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Builder
    public PostStack(Post post, Stack stack, Integer sortOrder) {
        this.post = post;
        this.stack = stack;
        this.sortOrder = sortOrder;
    }
}
