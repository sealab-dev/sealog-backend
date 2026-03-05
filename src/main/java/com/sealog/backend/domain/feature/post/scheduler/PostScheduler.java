package com.sealog.backend.domain.feature.post.scheduler;

import com.sealog.backend.domain.feature.post.service.PostSchedulerService;
import com.sealog.backend.global.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 게시글 정리 스케줄러
 *
 * 매일 자정에 실행되어 7일 이상 삭제 상태인 게시글을 영구 삭제합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostScheduler {

    private final PostSchedulerService postSchedulerService;

    /**
     * 삭제된 게시글 정리 작업
     *
     * 실행 주기: 매일 자정 (00:00:00)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupDeletedPosts() {
        LogUtils.runAndShowCostLog("게시글 정리 스케줄러", postSchedulerService::cleanupExpiredPosts);
    }
}
