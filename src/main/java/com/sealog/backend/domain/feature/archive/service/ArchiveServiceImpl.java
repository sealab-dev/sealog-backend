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
    public Page<ArchiveResponse.ArchiveItems> getPagedItemsByNicknameForGuest(String nickname, Pageable pageable) {

        return archiveRepository
                .findByUserNicknameAndIsPublic(nickname, true, pageable)
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
                .orElseThrow(() -> CustomException.notFound("존재하지 않거나 탈퇴한 사용자의 아카이브를 생성할 수 없습니다."));

        // 2. 검증
        String name = request.getName().trim();
        String slug = SlugUtils.generate(name);

        // 같은 이름 생성 시도 차단
        if (archiveRepository.existsByNameAndUserId(name, userId))
            throw CustomException.badRequest("이미 같은 이름의 아카이브가 존재합니다.");

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
        Archive archive = findArchiveByNicknameAndSlug(nickname, slug);

        // 2. 검증
        String prevName = archive.getName();
        String editName = request.getName().trim();

        // 타인 정보 수정 차단
        verifyOwner(archive, userId);

        // 동일 이름으로 수정 차단
        if (Objects.equals(prevName, editName))
            throw CustomException.badRequest("같은 이름으로 변경할 수 없습니다.");

        // 다른 아카이브와 이름 중복 차단
        if (archiveRepository.existsByNameAndUserId(editName, userId))
            throw CustomException.conflict("이미 같은 이름의 아카이브가 존재합니다.");

        // 3. 연관관계 메소드 기반 갱신
        archive.edit(editName, SlugUtils.generate(editName));
    }

    @Transactional
    @Override
    public void show(Long userId, String nickname, String slug) {

        // 1. 조회 및 검증
        Archive archive = findArchiveByNicknameAndSlug(nickname, slug);
        verifyOwner(archive, userId); // 타인 정보 수정 차단

        // 2. 공개 상태로 변경
        archive.editIsPublic(true);
    }


    @Transactional
    @Override
    public void hide(Long userId, String nickname, String slug) {

        // 1. 조회 및 검증
        Archive archive = findArchiveByNicknameAndSlug(nickname, slug);
        verifyOwner(archive, userId); // 타인 정보 수정 차단

        // 2. 비공개 상태로 변경
        archive.editIsPublic(false);
    }


    @Transactional
    @Override
    public void remove(Long userId, String nickname, String slug) {

        // 1. 조회 및 검증
        Archive archive = findArchiveByNicknameAndSlug(nickname, slug);
        verifyOwner(archive, userId); // 타인 정보 수정 차단

        // 2. 삭제 수행
        archiveRepository.delete(archive);
    }

    @Transactional
    @Override
    public void changePostArchive(Long userId, Long archiveId, Long postId) {

        // 1. Post 조회 및 검증
        Post post = findPostById(postId);
        verifyOwner(post, userId);

        // 2. Archive 조회 및 검증
        Archive archive = findArchiveById(archiveId);
        verifyOwner(archive, userId);

        // 3. 변경
        post.addToArchive(archive);
    }

    @Transactional
    @Override
    public void removePostArchive(Long userId, Long postId) {

        // 1. Post 조회 및 검증
        Post post = findPostById(postId);
        verifyOwner(post, userId);

        // 2. 아카이브 해제
        post.removeFromArchive();
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
     * 엔티티 조회 메소드
     */
    private Archive findArchiveByNicknameAndSlug(String nickname, String slug) {

        return archiveRepository
                .findByNicknameAndSlug(nickname, slug)
                .orElseThrow(() -> CustomException.notFound("존재하지 않거나 이미 삭제된 아카이브입니다."));
    }

    private Archive findArchiveById(Long id) {

        return archiveRepository
                .findById(id)
                .orElseThrow(() -> CustomException.notFound("존재하지 않거나 이미 삭제된 아카이브입니다."));
    }

    private Post findPostById(Long postId) {

        return postRepository
                .findById(postId)
                .orElseThrow(() -> CustomException.notFound("이미 삭제되었거나 존재하지 않는 게시글입니다."));
    }

    /**
     * 검증 메소드
     */
    private void verifyOwner(Archive archive, Long requestUserId) {

        if (!archive.isOwnedBy(requestUserId))
            throw CustomException.forbidden("다른 사용자의 아카이브를 변경할 수 없습니다.");
    }

    private void verifyOwner(Post post, Long requestUserId) {

        if (!post.isWrittenBy(requestUserId))
            throw CustomException.forbidden("다른 사용자의 게시글을 변경할 수 없습니다");
    }

}