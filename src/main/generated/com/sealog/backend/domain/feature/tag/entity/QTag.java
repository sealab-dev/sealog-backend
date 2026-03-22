package com.sealog.backend.domain.feature.tag.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTag is a Querydsl query type for Tag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTag extends EntityPathBase<Tag> {

    private static final long serialVersionUID = -836174081L;

    public static final QTag tag = new QTag("tag");

    public final com.sealog.backend.domain.base.entity.QBaseTimeEntity _super = new com.sealog.backend.domain.base.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTag(String variable) {
        super(Tag.class, forVariable(variable));
    }

    public QTag(Path<? extends Tag> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTag(PathMetadata metadata) {
        super(Tag.class, metadata);
    }

}

