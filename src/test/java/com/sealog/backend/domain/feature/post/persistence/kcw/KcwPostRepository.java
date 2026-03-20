package com.sealog.backend.domain.feature.post.persistence.kcw;

import com.sealog.backend.domain.feature.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface KcwPostRepository extends
        JpaRepository<Post, Long>, JpaSpecificationExecutor<Post>, KcwPostQuerydslRepository {
}
