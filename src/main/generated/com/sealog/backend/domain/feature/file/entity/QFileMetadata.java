package com.sealog.backend.domain.feature.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFileMetadata is a Querydsl query type for FileMetadata
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFileMetadata extends EntityPathBase<FileMetadata> {

    private static final long serialVersionUID = 1416265696L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFileMetadata fileMetadata = new QFileMetadata("fileMetadata");

    public final com.sealog.backend.domain.base.entity.QBaseTimeEntity _super = new com.sealog.backend.domain.base.entity.QBaseTimeEntity(this);

    public final StringPath contentType = createString("contentType");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath originalName = createString("originalName");

    public final StringPath path = createString("path");

    public final NumberPath<Long> size = createNumber("size", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.sealog.backend.domain.feature.user.entity.QUser user;

    public QFileMetadata(String variable) {
        this(FileMetadata.class, forVariable(variable), INITS);
    }

    public QFileMetadata(Path<? extends FileMetadata> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFileMetadata(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFileMetadata(PathMetadata metadata, PathInits inits) {
        this(FileMetadata.class, metadata, inits);
    }

    public QFileMetadata(Class<? extends FileMetadata> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.sealog.backend.domain.feature.user.entity.QUser(forProperty("user")) : null;
    }

}

