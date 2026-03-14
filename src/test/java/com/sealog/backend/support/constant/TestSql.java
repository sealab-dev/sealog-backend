package com.sealog.backend.support.constant;

import lombok.experimental.UtilityClass;

/**
 * 테스트 내에서 사용하는 Native SQL을 관리하는 클래스
 */
@UtilityClass
public class TestSql {

    public static final String FOREIGN_KEY_CHECKS_ACTIVATION = "SET FOREIGN_KEY_CHECKS = 1";
    public static final String FOREIGN_KEY_CHECKS_INACTIVATION = "SET FOREIGN_KEY_CHECKS = 0";
    public static final String TRUNCATE_TABLE = "TRUNCATE TABLE ";

    public static final String SELECT_TABLE_NAMES = """
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = ?
    """.strip();

    public static final String INSERT_USER = """
        INSERT INTO users (email, password, name, nickname, role) VALUES (?, ?, ?, ?, ?)
    """.strip();

    public static final String INSERT_POST = """
        INSERT INTO posts (user_id, title, slug, excerpt, content, status, thumbnail_path, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, null, NOW(), NOW())
    """.strip();

    public static final String INSERT_SERIES = """
        INSERT INTO series (user_id, name, slug, is_public, created_at, updated_at)
        VALUES (?, ?, ?, ?, NOW(), NOW())
    """.strip();
}
