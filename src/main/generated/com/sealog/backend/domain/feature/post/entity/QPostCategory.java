package com.sealog.backend.domain.feature.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostCategory is a Querydsl query type for PostCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostCategory extends EntityPathBase<PostCategory> {

    private static final long serialVersionUID = -1704416777L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostCategory postCategory = new QPostCategory("postCategory");

    public final com.sealog.backend.domain.feature.category.entity.QCategory category;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPost post;

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QPostCategory(String variable) {
        this(PostCategory.class, forVariable(variable), INITS);
    }

    public QPostCategory(Path<? extends PostCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostCategory(PathMetadata metadata, PathInits inits) {
        this(PostCategory.class, metadata, inits);
    }

    public QPostCategory(Class<? extends PostCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.sealog.backend.domain.feature.category.entity.QCategory(forProperty("category")) : null;
        this.post = inits.isInitialized("post") ? new QPost(forProperty("post"), inits.get("post")) : null;
    }

}

