package com.sealog.backend.support.constant;

import lombok.experimental.UtilityClass;

/**
 * 테스트 내에서 사용하는 Native SQL을 관리하는 클래스
 */
@UtilityClass
public class TestSql {

    public static final String INSERT_USER = """
        INSERT INTO users (email, password, name, nickname, role) VALUES (?, ?, ?, ?, ?)
    """;

    public static final String INSERT_POST = """
        INSERT INTO posts (user_id, title, slug, excerpt, content, status, thumbnail_path, created_at, updated_at) 
        VALUES (?, ?, ?, ?, ?, ?, null, NOW(), NOW())
    """;

    public static final String INSERT_ARCHIVE = """
        INSERT INTO archives (user_id, name, slug, is_public, created_at, updated_at)
        VALUES (?, ?, ?, ?, NOW(), NOW())
    """;

    public static final String INSERT_ARCHIVE_POST = """
        INSERT INTO archive_posts (archive_id, post_id, sort_order)
        VALUES (?, ?, ?)
    """;
}
