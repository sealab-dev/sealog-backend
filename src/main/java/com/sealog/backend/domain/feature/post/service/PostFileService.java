package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.file.support.FileUsageCollector;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * 게시글-파일 매핑 도메인 비즈니스 규약 인터페이스
 *
 * - 게시글과 파일 메타데이터 간의 연관 관계를 관리합니다.
 * - 썸네일 및 본문 이미지의 업로드와 매핑 정합성을 보장합니다.
 */
public interface PostFileService extends FileUsageCollector {

    // ========== 오케스트레이션 (Upload + Mapping) ========== //

    /**
     * 게시글의 썸네일을 업로드하고 연결 정보를 저장합니다.
     * - 기존 썸네일 매핑이 있다면 제거 후 교체합니다.
     *
     * @param postId    게시글 ID
     * @param user      업로드 수행 사용자
     * @param thumbnail 업로드할 썸네일 파일
     * @return 저장된 썸네일의 스토리지 경로
     * @throws CustomException 파일 업로드 실패 또는 DB 저장 실패 시 발생
     */
    String saveThumbnailFile(Long postId, User user, MultipartFile thumbnail);

    /**
     * 게시글 생성 시 본문에 포함된 파일들의 매핑 정보를 저장합니다.
     *
     * @param postId      게시글 ID
     * @param cleanedHtml 정제된 본문 HTML (여기서 파일 ID 추출)
     */
    void saveContentFileMappings(Long postId, String cleanedHtml);

    /**
     * 게시글 수정 시 본문에 포함된 파일들의 매핑 정보를 업데이트합니다.
     * - HTML 내 파일 ID를 추출하여 추가/삭제된 파일을 매핑 테이블에 반영합니다.
     *
     * @param postId      게시글 ID
     * @param cleanedHtml 정제된 본문 HTML
     */
    void updateContentFileMappings(Long postId, String cleanedHtml);

    // ========== 저수준 매핑 관리 (CRUD) ========== //

    /**
     * 썸네일 파일 매핑을 직접 저장합니다.
     */
    void saveThumbnail(Long postId, Long fileId);

    /**
     * 여러 본문 파일들의 매핑을 일괄 저장합니다.
     */
    void saveContentFiles(Long postId, List<Long> fileIds);

    /**
     * 게시글의 썸네일 매핑 정보를 삭제합니다.
     */
    void deleteThumbnail(Long postId);

    /**
     * 게시글 본문 파일들 중 지정된 ID 목록에 해당하는 매핑을 삭제합니다.
     */
    void deleteContentFiles(Long postId, List<Long> fileIds);

    /**
     * 게시글과 관련된 모든 파일 매핑 정보를 삭제합니다. (게시글 영구 삭제 시 호출)
     */
    void deleteAllMappings(Long postId);

    /**
     * 게시글 본문에서 사용 중인 모든 파일 ID 목록을 조회합니다.
     */
    Set<Long> getContentFileIds(Long postId);
}
