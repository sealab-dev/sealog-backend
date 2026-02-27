package com.sealog.backend.domain.feature.user.entity;

import com.sealog.backend.domain.base.entity.BaseTimeEntity;
import com.sealog.backend.domain.feature.user.enums.SocialType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_social_links",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_social_type",
                columnNames = {"user_id", "social_type"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSocial extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 30)
    private SocialType socialType;

    @Column(nullable = false, length = 500)
    private String url;

    @Builder
    public UserSocial(User user, SocialType socialType, String url) {
        this.user = user;
        this.socialType = socialType;
        this.url = url;
    }

    // === 비즈니스 로직 === //

    public void updateUrl(String url) {
        this.url = url;
    }
}
