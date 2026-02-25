package com.sealog.backend.domain.feature.file.verifier;

import com.sealog.backend.domain.feature.file.repository.FileMetadataRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * 파일 메타데이터 관리 서비스 구현체
 *
 * 설계 원칙:
 * - 순수 메타데이터 CRUD만 담당
 * - S3 업로드/삭제는 호출하지 않음 (비즈니스 로직에서 처리)
 * - 트랜잭션은 상위 레이어에서 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileMetadataVerifierImpl implements FileMetadataVerifier {

    // 사용 의존성
    private final FileMetadataRepository fileMetadataRepository;


    @Override
    public void verifyExist(Long fileId) {

        // 유효하지 않은 번호는 검증을 수행하지 않음
        if (Objects.isNull(fileId)) return;

        // 카운팅 수행 및 검증
        processCountAndVerify(List.of(fileId));
    }


    @Override
    public void verifyAllExist(List<Long> fileIds) {

        // 비어있는 경우, 검증 미수행
        if (CollectionUtils.isEmpty(fileIds)) return;

        // 카운팅 수행 및 검증
        processCountAndVerify(fileIds);
    }


    /**
     * 카운팅 수행 및 검증
     */
    private void processCountAndVerify(List<Long> fileIds) {

        // 존재 수량을 확인 후, 개수 대조 후 결과 반환
        long existingCount = fileMetadataRepository.countByIdIn(fileIds);

        // 카운트 수향 비교
        if (existingCount != fileIds.size()) {
            log.error("일부 파일을 찾을 수 없음: 요청={}, 존재={}", fileIds.size(), existingCount);
            throw new CustomException("일부 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
    }


}