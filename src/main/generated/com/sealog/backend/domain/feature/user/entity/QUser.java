package com.sealog.backend.domain.feature.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1923919791L;

    public static final QUser user = new QUser("user");

    public final com.sealog.backend.domain.base.entity.QBaseTimeEntity _super = new com.sealog.backend.domain.base.entity.QBaseTimeEntity(this);

    public final StringPath about = createString("about");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final StringPath position = createString("position");

    public final ListPath<com.sealog.backend.domain.feature.post.entity.Post, com.sealog.backend.domain.feature.post.entity.QPost> posts = this.<com.sealog.backend.domain.feature.post.entity.Post, com.sealog.backend.domain.feature.post.entity.QPost>createList("posts", com.sealog.backend.domain.feature.post.entity.Post.class, com.sealog.backend.domain.feature.post.entity.QPost.class, PathInits.DIRECT2);

    public final StringPath profileImagePath = createString("profileImagePath");

    public final EnumPath<com.sealog.backend.domain.feature.user.enums.UserRole> role = createEnum("role", com.sealog.backend.domain.feature.user.enums.UserRole.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

