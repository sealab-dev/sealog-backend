package com.sealog.backend.domain.feature.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOwnerFileEntity is a Querydsl query type for OwnerFileEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QOwnerFileEntity extends EntityPathBase<OwnerFileEntity> {

    private static final long serialVersionUID = 34490461L;

    public static final QOwnerFileEntity ownerFileEntity = new QOwnerFileEntity("ownerFileEntity");

    public final com.sealog.backend.domain.base.entity.QBaseTimeEntity _super = new com.sealog.backend.domain.base.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final NumberPath<Long> fileId = createNumber("fileId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QOwnerFileEntity(String variable) {
        super(OwnerFileEntity.class, forVariable(variable));
    }

    public QOwnerFileEntity(Path<? extends OwnerFileEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOwnerFileEntity(PathMetadata metadata) {
        super(OwnerFileEntity.class, metadata);
    }

}

