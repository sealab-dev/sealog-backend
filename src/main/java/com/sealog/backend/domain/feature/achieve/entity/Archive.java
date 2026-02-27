package com.sealog.backend.domain.feature.achieve.entity;

import com.sealog.backend.domain.base.entity.BaseTimeEntity;
import com.sealog.backend.domain.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "archives")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Archive extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * URL-safe한 컬렉션 식별자
     * - 이름 기반으로 자동 생성
     * - UNIQUE 제약조건
     * - 조회 시 ID 대신 사용
     */
    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Builder
    public Archive(User user, String name, String slug, boolean isPublic) {
        this.user = user;
        this.name = name;
        this.slug = slug;
        this.isPublic = isPublic;
    }

    // === 비즈니스 로직 === //

    public void updateName(String name) {
        this.name = name;
    }

    public void updateSlug(String slug) {
        this.slug = slug;
    }

    public void updateVisibility(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }
}
