package com.sealog.backend.domain.feature.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserFile is a Querydsl query type for UserFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserFile extends EntityPathBase<UserFile> {

    private static final long serialVersionUID = -394221109L;

    public static final QUserFile userFile = new QUserFile("userFile");

    public final com.sealog.backend.domain.base.entity.QBaseTimeEntity _super = new com.sealog.backend.domain.base.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final NumberPath<Long> fileId = createNumber("fileId", Long.class);

    public final EnumPath<com.sealog.backend.domain.feature.user.enums.UserFileType> fileType = createEnum("fileType", com.sealog.backend.domain.feature.user.enums.UserFileType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserFile(String variable) {
        super(UserFile.class, forVariable(variable));
    }

    public QUserFile(Path<? extends UserFile> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserFile(PathMetadata metadata) {
        super(UserFile.class, metadata);
    }

}

