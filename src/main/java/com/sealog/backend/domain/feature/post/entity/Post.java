package com.sealog.backend.domain.feature.post.entity;

import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.base.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "posts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_user_title", columnNames = {"user_id", "title"}),
                @UniqueConstraint(name = "uk_post_user_slug", columnNames = {"user_id", "slug"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String title;

    @Column(nullable = false, length = 200)
    private String slug;

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "thumbnail_path", length = 1000)
    private String thumbnailPath;

    // === 생성자 === //
    @Builder
    public Post(User user, String title, String slug, String excerpt,
                String content, PostStatus status, String thumbnailPath) {
        this.user = user;
        this.title = title;
        this.slug = slug;
        this.excerpt = excerpt;
        this.content = content;
        this.status = status;
        this.thumbnailPath = thumbnailPath;
    }

    // ============== 업데이트 관리 ============== //
    /**
     * 게시글 수정
     */
    public void update(String title, String slug, String excerpt, String content) {
        this.title = title;
        this.slug = slug;
        this.excerpt = excerpt;
        this.content = content;
    }

    // ============== 상태 관리 ============== //
    /**
     * 발행됨 상태로 변경
     */
    public void publish() {
        this.status = PostStatus.PUBLISHED;
    }

    /**
     * 비공개 상태로 변경
     */
    public void unpublish() {
        this.status = PostStatus.PRIVATE;
    }

    // ============== 삭제 관리 ============== //
    /**
     * 소프트 삭제 처리
     * - status를 DELETED로 변경
     * - deletedAt 시간 기록 (BaseTimeEntity)
     */
    public void softDelete() {
        this.markAsDeleted();
    }

    /**
     * 삭제 복구
     * - status를 PUBLISHED로 변경
     * - deletedAt 초기화 (BaseTimeEntity)
     */
    public void restoreFromDelete() {
        this.restore();
    }

    // ============== 작성자 확인 ============== //
    /**
     * 게시글 작성자가 맞는지 확인
     */
    public boolean isWrittenBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    // ============== 썸네일 관리 ============== //
    /**
     * 썸네일 URL 업데이트
     */
    public void updateThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    /**
     * 썸네일 제거
     */
    public void removeThumbnailPath() {
        this.thumbnailPath = null;
    }
}