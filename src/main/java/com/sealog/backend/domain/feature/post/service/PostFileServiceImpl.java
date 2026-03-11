package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.file.entity.FileMetadata;
import com.sealog.backend.domain.feature.file.service.FileMetadataService;
import com.sealog.backend.domain.feature.file.util.FileValidator;
import com.sealog.backend.domain.feature.post.entity.PostFile;
import com.sealog.backend.domain.feature.post.enums.PostFileType;
import com.sealog.backend.domain.feature.post.repository.PostFileRepository;
import com.sealog.backend.domain.feature.post.util.PostHtmlParser;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.dto.FileUploadResult;
import com.sealog.backend.infra.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostFileServiceImpl implements PostFileService {

    private final PostFileRepository postFileRepository;
    private final FileStorageService fileStorageService;
    private final FileMetadataService fileMetadataService;

    // ========== 썸네일 오케스트레이션 ========== //

    /**
     * 썸네일을 S3에 업로드하고 메타데이터와 게시글을 연결합니다.
     * 처리 순서: 유효성 검증 → S3 업로드 → file_metadata 저장 → post_file 매핑 저장
     * S3 업로드 성공 후 DB 저장이 실패하면 S3 파일을 삭제해 정합성을 보장합니다.
     *
     * @return 저장된 썸네일 경로 (post.thumbnailPath 업데이트용으로 PostServiceImpl에 반환)
     */
    /**
     * 썸네일을 업로드하고 게시글에 연결합니다.
     * 기존 썸네일 매핑이 있으면 삭제 후 교체하고, 없으면 그냥 업로드합니다.
     * S3 업로드 성공 후 DB 저장이 실패하면 S3 파일을 삭제해 정합성을 보장합니다.
     *
     * @return 저장된 썸네일 경로 (post.thumbnailPath 업데이트용으로 PostServiceImpl에 반환)
     */
    @Override
    @Transactional
    public String saveThumbnailFile(Long postId, User user, MultipartFile thumbnail) {
        FileValidator.validateImageFile(thumbnail);

        deleteThumbnail(postId); // 기존 매핑 없으면 no-op

        FileUploadResult uploadResult;
        try {
            uploadResult = fileStorageService.uploadPublicThumbnail(thumbnail);
        } catch (IOException e) {
            throw CustomException.badRequest("썸네일 업로드에 실패했습니다: " + e.getMessage());
        }

        try {
            FileMetadata fileMetadata = fileMetadataService.upload(uploadResult, user);
            saveThumbnail(postId, fileMetadata.getId());
            log.info("썸네일 업로드 완료: postId={}, path={}", postId, uploadResult.path());
            return uploadResult.path();
        } catch (Exception e) {
            fileStorageService.deleteFile(uploadResult.path());
            log.error("썸네일 DB 저장 실패로 S3 파일 롤백: path={}", uploadResult.path());
            throw e;
        }
    }

    // ========== 본문 파일 오케스트레이션 ========== //

    /**
     * 게시글 생성 후: 이미 정제된 HTML에서 파일 ID를 추출해 post_file 매핑을 저장합니다.
     * cleanContentHtml 호출 후 게시글 저장이 완료된 시점에 호출합니다.
     */
    @Override
    @Transactional
    public void saveContentFileMappings(Long postId, String refinedHtml) {
        Set<Long> fileIds = PostHtmlParser.extractFileIds(refinedHtml);

        if (fileIds.isEmpty()) {
            log.debug("게시글 생성 - 본문에 파일 참조 없음: postId={}", postId);
            return;
        }

        saveContentFiles(postId, new ArrayList<>(fileIds));
        log.debug("게시글 생성 - 본문 파일 매핑 완료: postId={}, count={}", postId, fileIds.size());
    }

    /**
     * 게시글 수정 후: 이미 정제된 HTML을 기준으로 파일 매핑을 증분 업데이트합니다.
     * - 본문에서 제거된 파일: post_file 매핑 삭제 → 고아 파일 스케줄러 정리 대상으로 전환
     * - 본문에 새로 추가된 파일: 매핑 저장 (유효성 검증 없음, cleanContentHtml에서 이미 처리)
     * - 변경 없는 파일: 그대로 유지
     */
    @Override
    @Transactional
    public void updateContentFileMappings(Long postId, String cleanedHtml) {
        Set<Long> oldFileIds = getContentFileIds(postId);
        Set<Long> newFileIds = PostHtmlParser.extractFileIds(cleanedHtml);

        Set<Long> fileIdsToDelete = new HashSet<>(oldFileIds);
        fileIdsToDelete.removeAll(newFileIds);

        Set<Long> fileIdsToAdd = new HashSet<>(newFileIds);
        fileIdsToAdd.removeAll(oldFileIds);

        log.info("게시글 수정 - 파일 매핑 변경 분석: postId={}, 기존={}, 신규={}, 삭제={}, 추가={}",
                postId, oldFileIds.size(), newFileIds.size(),
                fileIdsToDelete.size(), fileIdsToAdd.size());

        if (!fileIdsToDelete.isEmpty()) {
            deleteContentFiles(postId, new ArrayList<>(fileIdsToDelete));
        }

        if (!fileIdsToAdd.isEmpty()) {
            saveContentFiles(postId, new ArrayList<>(fileIdsToAdd));
        }
    }

    // ========== 매핑 CRUD ========== //

    @Override
    @Transactional
    public void saveThumbnail(Long postId, Long fileId) {
        PostFile thumbnailMapping = PostFile.ofThumbnail(postId, fileId);
        postFileRepository.save(thumbnailMapping);
        log.info("썸네일 매핑 저장 완료: postId={}, fileId={}", postId, fileId);
    }

    @Override
    @Transactional
    public void saveContentFiles(Long postId, List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.debug("저장할 본문 파일이 없음: postId={}", postId);
            return;
        }

        List<PostFile> contentMappings = fileIds.stream()
                .map(fileId -> PostFile.ofContent(postId, fileId))
                .collect(Collectors.toList());

        postFileRepository.saveAll(contentMappings);
        log.info("본문 파일 매핑 저장 완료: postId={}, count={}", postId, contentMappings.size());
    }

    @Override
    @Transactional
    public void deleteThumbnail(Long postId) {
        Optional<PostFile> existingThumbnail = postFileRepository
                .findTopByPostIdAndFileType(postId, PostFileType.THUMBNAIL);

        if (existingThumbnail.isPresent()) {
            postFileRepository.delete(existingThumbnail.get());
            log.debug("기존 썸네일 매핑 삭제 완료: postId={}, fileId={}",
                    postId, existingThumbnail.get().getFileId());
        } else {
            log.debug("삭제할 썸네일 매핑이 없음: postId={}", postId);
        }
    }

    @Override
    @Transactional
    public void deleteContentFiles(Long postId, List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.debug("삭제할 본문 파일이 없음: postId={}", postId);
            return;
        }

        List<PostFile> existingMappings = postFileRepository
                .findByPostIdAndFileType(postId, PostFileType.CONTENT);

        List<Long> toDeleteIds = existingMappings.stream()
                .filter(pf -> fileIds.contains(pf.getFileId()))
                .map(PostFile::getId)
                .collect(Collectors.toList());

        if (!toDeleteIds.isEmpty()) {
            postFileRepository.deleteAllById(toDeleteIds);
            log.info("본문 파일 매핑 삭제 완료: postId={}, deletedCount={}", postId, toDeleteIds.size());
        } else {
            log.debug("삭제할 본문 파일 매핑이 없음: postId={}", postId);
        }
    }

    @Override
    @Transactional
    public void deleteAllMappings(Long postId) {
        int deletedCount = postFileRepository.deleteByPostId(postId);
        log.info("게시글의 모든 파일 매핑 삭제 완료: postId={}, deletedCount={}", postId, deletedCount);
    }

    @Override
    public Set<Long> getContentFileIds(Long postId) {
        Set<Long> fileIds = postFileRepository
                .findFileIdsByPostIdAndFileType(postId, PostFileType.CONTENT);
        log.debug("본문 파일 ID 조회 완료: postId={}, count={}", postId, fileIds.size());
        return fileIds;
    }

    @Override
    public Set<Long> collectUsedFileIds() {
        List<Long> usedFileIds = postFileRepository.findAllUsedFileIds();
        Set<Long> uniqueFileIds = Set.copyOf(usedFileIds);
        log.info("PostFile에서 사용 중인 파일 ID 수집 완료: count={}", uniqueFileIds.size());
        return uniqueFileIds;
    }
}