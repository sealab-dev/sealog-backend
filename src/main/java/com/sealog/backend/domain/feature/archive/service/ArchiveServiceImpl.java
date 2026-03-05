package com.sealog.backend.domain.feature.archive.service;

import com.sealog.backend.domain.base.util.SlugUtils;
import com.sealog.backend.domain.feature.archive.dto.ArchiveRequest;
import com.sealog.backend.domain.feature.archive.dto.ArchiveResponse;
import com.sealog.backend.domain.feature.archive.entity.Archive;
import com.sealog.backend.domain.feature.archive.repository.ArchiveRepository;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.enums.PostStatus;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * ArchiveService 구현 클래스
 */
@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl implements ArchiveService {

    // 사용 의존성
    private final ArchiveRepository archiveRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public Page<ArchiveResponse.ArchiveItems> getPagedPublicItemsByNickname(String nickname, Pageable pageable) {

        return archiveRepository
                .findPublicByUserNickname(nickname, pageable)
                .map(this::toItems);
    }

    @Override
    public Page<ArchiveResponse.PostItems> getPagedPostItemsByArchiveIdForGuest(Long archiveId, Pageable pageable) {

        return postRepository
                .findByArchiveIdAndStatus(archiveId, PostStatus.PUBLISHED, pageable)
                .map(this::toPostItems);
    }

    @Override
    public Page<ArchiveResponse.ArchiveItems> getPagedItemsForUser(Long userId, Pageable pageable) {

        return archiveRepository
                .findByUserId(userId, pageable)
                .map(this::toItems);
    }

    @Override
    public Page<ArchiveResponse.PostItems> getPagedPostItemsByUserIdAndArchiveIdForUser(Long userId, Long archiveId, Pageable pageable) {

        return postRepository
                .findByUserIdAndArchiveId(userId, archiveId, pageable)
                .map(this::toPostItems);
    }

    @Transactional
    @Override
    public void add(Long userId, ArchiveRequest.Add request) {

        // 1. 회원 엔티티 조회
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new CustomException("존재하지 않거나 탈퇴한 사용자의 아카이브를 생성할 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR));

        // 2. 검증
        String name = request.getName().trim();
        String slug = SlugUtils.generate(name);

        // 같은 이름 생성 시도 차단
        if (archiveRepository.existsByNameAndUserId(name, userId))
            throw new CustomException("이미 같은 이름의 아카이브가 존재합니다.", HttpStatus.BAD_REQUEST);

        // 3. entity 생성
        Archive archive = Archive.builder()
                .user(user)
                .name(name)
                .slug(slug)
                .isPublic(true)
                .build();

        // 4. 저장
        archiveRepository.save(archive);
    }

    @Transactional
    @Override
    public void edit(Long userId, String nickname, String slug, ArchiveRequest.Edit request) {

        // 1. entity 조회
        Archive archive = findEntityByNicknameAndSlug(nickname, slug);

        // 2. 검증
        String prevName = archive.getName();
        String editName = request.getName().trim();

        // 타인 정보 수정 차단
        verifyOwner(archive, userId);

        // 동일 이름으로 수정 차단
        if (Objects.equals(prevName, editName))
            throw new CustomException("같은 이름으로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);

        // 3. 연관관계 메소드 기반 갱신
        archive.edit(editName, SlugUtils.generate(editName));
    }

    @Transactional
    @Override
    public void show(Long userId, String nickname, String slug) {

        // 1. 조회
        Archive archive = findEntityByNicknameAndSlug(nickname, slug);

        // 2. 검증
        verifyOwner(archive, userId); // 타인 정보 수정 차단

        // 3. 공개상태로 변경
        archive.editIsPublic(true);
    }


    @Transactional
    @Override
    public void hide(Long userId, String nickname, String slug) {

        // 1. 조회
        Archive archive = findEntityByNicknameAndSlug(nickname, slug);

        // 2. 검증
        verifyOwner(archive, userId); // 타인 정보 수정 차단

        // 3. 비공개상태로 변경
        archive.editIsPublic(false);
    }


    @Transactional
    @Override
    public void remove(Long userId, String nickname, String slug) {

        // 1. 조회
        Archive archive = findEntityByNicknameAndSlug(nickname, slug);

        // 2. 검증
        verifyOwner(archive, userId); // 타인 정보 수정 차단

        // 3. 삭제 수행
        archiveRepository.delete(archive);
    }


    /**
     * Entity -> DTO 변환 메소드
     */
    private ArchiveResponse.ArchiveItems toItems(Archive entity) {
        return ArchiveResponse.ArchiveItems.of(entity.getId(), entity.getSlug(), entity.getName());
    }

    private ArchiveResponse.PostItems toPostItems(Post entity) {
        return ArchiveResponse.PostItems.of(entity.getId(), entity.getTitle(), entity.getSlug(), entity.getThumbnailPath());
    }


    /**
     * nickname, userId slug 아카이브 조회
     */
    private Archive findEntityByNicknameAndSlug(String nickname, String slug) {

        return archiveRepository
                .findByNicknameAndSlug(nickname, slug)
                .orElseThrow(() -> new CustomException("존재하지 않거나 이미 삭제된 아카이브입니다.", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * 수정 전 사용자 검증
     */
    private void verifyOwner(Archive archive, Long requestUserId) {

        if (!archive.isOwnedBy(requestUserId))
            throw new CustomException("다른 사용자의 아카이브를 변경할 수 없습니다.", HttpStatus.FORBIDDEN);
    }

}
