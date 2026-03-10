package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.file.support.FileUsageCollector;
import com.sealog.backend.domain.feature.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * PostFile 중간 테이블 관리 서비스
 *
 * 역할:
 * - Post와 FileMetadata 간의 매핑 관리
 * - 썸네일 업로드/교체 오케스트레이션 (유효성 검증 → S3 업로드 → 메타데이터 저장 → 매핑)
 * - 본문 파일 동기화 오케스트레이션 (생성/수정 시 증분 업데이트)
 * - 게시글이 실제 사용하는 파일 조회
 */
public interface PostFileService extends FileUsageCollector {

    // ========== 썸네일 오케스트레이션 ========== //

    /**
     * 썸네일을 업로드하고 게시글에 연결합니다.
     * 기존 썸네일이 있으면 매핑을 제거하고 교체합니다 (없으면 no-op).
     * 처리 순서: 유효성 검증 → 기존 매핑 삭제 → S3 업로드 → file_metadata 저장 → post_file 매핑
     * S3 업로드 성공 후 DB 저장 실패 시 S3 파일을 삭제해 정합성을 보장합니다.
     *
     * @param postId    게시글 ID
     * @param user      업로드 요청 사용자
     * @param thumbnail 썸네일 파일
     * @return 저장된 썸네일 경로 (post.thumbnailPath 업데이트용)
     */
    String saveThumbnailFile(Long postId, User user, MultipartFile thumbnail);

    // ========== 본문 파일 오케스트레이션 ========== //

    /**
     * 게시글 생성 후: 이미 정제된 본문 HTML에서 파일 ID를 추출해 post_file 매핑을 저장합니다.
     * cleanContentHtml 호출 후 게시글 저장이 완료된 시점에 호출합니다.
     *
     * @param postId      게시글 ID
     * @param cleanedHtml cleanContentHtml이 반환한 최종 HTML
     */
    void saveContentFileMappings(Long postId, String cleanedHtml);

    /**
     * 게시글 수정 후: 이미 정제된 본문 HTML을 기준으로 파일 매핑을 증분 업데이트합니다.
     * 제거된 파일은 매핑 삭제, 추가된 파일은 매핑 저장합니다. (유효성 검증 없음)
     *
     * @param postId      게시글 ID
     * @param cleanedHtml cleanContentHtml이 반환한 최종 HTML
     */
    void updateContentFileMappings(Long postId, String cleanedHtml);

    // ========== 매핑 CRUD ========== //

    /**
     * 썸네일 파일 매핑 생성
     */
    void saveThumbnail(Long postId, Long fileId);

    /**
     * 본문 파일 매핑 생성 (여러 개)
     */
    void saveContentFiles(Long postId, List<Long> fileIds);

    /**
     * 게시글의 기존 썸네일 매핑 삭제
     */
    void deleteThumbnail(Long postId);

    /**
     * 게시글의 특정 본문 파일 매핑들 삭제
     */
    void deleteContentFiles(Long postId, List<Long> fileIds);

    /**
     * 게시글의 모든 파일 매핑 삭제 (게시글 삭제 시)
     */
    void deleteAllMappings(Long postId);

    /**
     * 게시글이 현재 사용 중인 본문 파일 ID 목록 조회
     */
    Set<Long> getContentFileIds(Long postId);
}