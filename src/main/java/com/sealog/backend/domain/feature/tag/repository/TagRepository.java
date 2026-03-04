package com.sealog.backend.domain.feature.tag.repository;

import com.sealog.backend.domain.feature.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 태그명으로 태그 조회 (findOrCreate 패턴용)
     */
    Optional<Tag> findByName(String name);
}
