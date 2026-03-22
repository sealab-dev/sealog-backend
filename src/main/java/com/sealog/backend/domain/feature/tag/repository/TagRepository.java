package com.sealog.backend.domain.feature.tag.repository;

import com.sealog.backend.domain.feature.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 태그 엔티티 리포지토리
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 태그명으로 태그 정보를 조회합니다.
     */
    Optional<Tag> findByName(String name);
}
