package com.sealog.backend.infra.storage.service;

import com.sealog.backend.infra.storage.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 파일 저장소 서비스 인터페이스
 * 프로필에 따라 S3 또는 로컬 저장소 구현체가 선택됨
 */
public interface FileStorageService {

    /**
     * 이미지를 저장소에 업로드
     *
     * @param file 업로드할 이미지 파일
     * @return FileUploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileUploadResult uploadPublicImage(MultipartFile file) throws IOException;

    /**
     * 동영상을 저장소에 업로드
     *
     * @param file 업로드할 동영상 파일
     * @return FileUploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileUploadResult uploadPublicVideo(MultipartFile file) throws IOException;

    /**
     * 문서를 저장소에 업로드
     *
     * @param file 업로드할 문서 파일
     * @return FileUploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileUploadResult uploadPublicDocument(MultipartFile file) throws IOException;

    /**
     * 오디오를 저장소에 업로드
     *
     * @param file 업로드할 오디오 파일
     * @return FileUploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileUploadResult uploadPublicAudio(MultipartFile file) throws IOException;

    /**
     * 압축 파일을 저장소에 업로드
     *
     * @param file 업로드할 압축 파일
     * @return FileUploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileUploadResult uploadPublicArchive(MultipartFile file) throws IOException;

    /**
     * 시스템 정적 자원을 저장소에 업로드
     *
     * @param file 업로드할 에셋 파일
     * @return FileUploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileUploadResult uploadPublicAssets(MultipartFile file) throws IOException;

    /**
     * 파일을 저장소에 업로드 (자동 타입 분류)
     * 저장 타입은 내부적으로 분류됨
     *
     * @param file 업로드할 파일
     * @return FileUploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    FileUploadResult uploadFile(MultipartFile file) throws IOException;

    /**
     * 저장소에 업로드된 파일을 삭제
     *
     * @param fileKey 삭제할 파일의 키
     */
    void deleteFile(String fileKey);

    /**
     * 저장소에 업로드된 여러 파일을 일괄 삭제
     *
     * @param fileKeys 삭제할 파일들의 키 목록
     * @return 삭제 성공한 파일 키 목록 (실패한 파일은 제외됨)
     */
    List<String> deleteFiles(List<String> fileKeys);

    /**
     * 파일의 접근 가능한 URL을 생성
     *
     * @param fileKey 조회할 파일의 키
     * @param minutes URL 유효 시간(분)
     * @return 접근 가능한 URL
     */
    String getPresignedUrl(String fileKey, int minutes);
}
