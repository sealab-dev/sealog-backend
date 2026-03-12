package com.sealog.backend.domain.feature.user.service;

import com.sealog.backend.domain.feature.user.dto.UserRequest;
import com.sealog.backend.domain.feature.user.dto.UserResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.entity.UserSocial;
import com.sealog.backend.domain.feature.user.enums.SocialType;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.domain.feature.user.repository.UserSocialRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSocialServiceImpl implements UserSocialService {

    private final UserSocialRepository userSocialRepository;
    private final UserRepository userRepository;

    @Override
    public List<UserResponse.SocialLinkItem> getMyLinks(Long userId) {
        return userSocialRepository.findAllByUserId(userId).stream()
                .map(UserResponse.SocialLinkItem::from)
                .toList();
    }

    @Override
    @Transactional
    public List<UserResponse.SocialLinkItem> update(Long userId, List<UserRequest.UpdateSocialLink> links) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        // 2. 중복 소셜 타입 검증
        List<SocialType> socialTypes = links.stream()
                .map(UserRequest.UpdateSocialLink::getSocialType)
                .toList();

        Set<SocialType> uniqueTypes = Set.copyOf(socialTypes);
        if (uniqueTypes.size() != socialTypes.size()) {
            throw CustomException.badRequest("동일한 소셜 타입이 중복되었습니다");
        }

        // 3. 기존 소셜 링크 전체 삭제
        userSocialRepository.deleteAllByUserId(userId);
        log.info("기존 소셜 링크 삭제 완료: userId={}", userId);

        // 4. 새 소셜 링크 저장
        List<UserSocial> newLinks = links.stream()
                .map(item -> UserSocial.builder()
                        .user(user)
                        .socialType(item.getSocialType())
                        .url(item.getUrl())
                        .build())
                .toList();

        List<UserSocial> savedLinks = userSocialRepository.saveAll(newLinks);
        log.info("소셜 링크 upsert 완료: userId={}, count={}", userId, savedLinks.size());

        return savedLinks.stream()
                .map(UserResponse.SocialLinkItem::from)
                .toList();
    }

    @Override
    public List<UserResponse.SocialLinkItem> getPublicLinks(String nickname) {
        List<UserSocial> links = userSocialRepository.findAllByUser_Nickname(nickname);

        if (links.isEmpty()) {
            log.info("소셜 링크 없음: nickname={}", nickname);
        }

        return links.stream()
                .map(UserResponse.SocialLinkItem::from)
                .toList();
    }
}
