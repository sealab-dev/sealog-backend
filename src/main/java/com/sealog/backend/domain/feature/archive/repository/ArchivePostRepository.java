package com.sealog.backend.domain.feature.archive.repository;

import com.sealog.backend.domain.feature.archive.entity.ArchivePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


/**
 * ArchivePost Entity Spring Data JPA 인터페이스
 */
public interface ArchivePostRepository extends JpaRepository<ArchivePost, Long> {

    @Query("""
        SELECT ap
        FROM ArchivePost ap
        LEFT JOIN FETCH ap.archive a
        WHERE ap.post.id = :postId AND a.isPublic = :isPublic
        ORDER BY ap.sortOrder ASC
    """)
    Page<ArchivePost> findByPostIdAndArchiveIsPublic(Long postId, boolean isPublic, Pageable pageable);


    @Query("""
        SELECT ap
        FROM ArchivePost ap
        LEFT JOIN FETCH ap.archive
        WHERE ap.id = :archivePostId
    """)
    Optional<ArchivePost> findByIdWithPost(Long archivePostId);
}