package com.sealog.backend.domain.feature.series.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeries is a Querydsl query type for Series
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSeries extends EntityPathBase<Series> {

    private static final long serialVersionUID = -1788121081L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeries series = new QSeries("series");

    public final com.sealog.backend.domain.base.entity.QBaseTimeEntity _super = new com.sealog.backend.domain.base.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPublic = createBoolean("isPublic");

    public final StringPath name = createString("name");

    public final StringPath slug = createString("slug");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.sealog.backend.domain.feature.user.entity.QUser user;

    public QSeries(String variable) {
        this(Series.class, forVariable(variable), INITS);
    }

    public QSeries(Path<? extends Series> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeries(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeries(PathMetadata metadata, PathInits inits) {
        this(Series.class, metadata, inits);
    }

    public QSeries(Class<? extends Series> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.sealog.backend.domain.feature.user.entity.QUser(forProperty("user")) : null;
    }

}

