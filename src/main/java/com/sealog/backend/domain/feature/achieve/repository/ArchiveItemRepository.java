package com.sealog.backend.domain.feature.achieve.repository;

import com.sealog.backend.domain.feature.achieve.entity.ArchivePost;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * ArchiveItem Entity Spring Data JPA 인터페이스
 */
public interface ArchiveItemRepository extends JpaRepository<ArchivePost, Long> {

}