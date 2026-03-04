package com.sealog.backend.domain.feature.post.service;

/**
 * 게시글 스케줄러 비즈니스 로직 인터페이스
 */
public interface PostSchedulerService {

    /**
     * 삭제 기간이 만료된 게시글 영구 삭제
     */
    void cleanupExpiredPosts();
}
