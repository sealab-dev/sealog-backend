package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.post.dto.PostMeResponse;
import com.sealog.backend.domain.feature.post.dto.PostMeRequest;
import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.global.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 게시글 서비스 인터페이스
 *
 * 공개(Guest) 및 인증(User) 게시글 관련 비즈니스 로직
 */
public interface PostService {

    // ========== Read ========== //
    /**
     * 특정 유저의 공개 게시글 목록 조회
     * - PUBLISHED 상태만 조회
     *
     * @param nickname 조회할 유저 닉네임
     * @param pageable 페이지네이션 정보
     * @return 게시글 목록
     */
    Page<PostResponse.PostItem> getUserPosts(String nickname, Pageable pageable);

    /**
     * 전체 공개 게시글 목록 조회
     * - PUBLISHED 상태만 조회
     *
     * @param pageable 페이지네이션 정보
     * @return 게시글 목록
     */
    Page<PostResponse.PostItem> getPosts(Pageable pageable);

    /**
     * 게시글 상세 조회 (Nickname + Slug 기반)
     * - PUBLISHED 상태만 조회
     *
     * @param nickname 작성자 닉네임
     * @param slug 조회할 게시글의 slug
     * @return 게시글 상세 정보 (관련 게시글 포함)
     * @throws CustomException 게시글을 찾을 수 없거나 작성자가 일치하지 않는 경우
     */
    PostResponse.PostDetail getDetail(String nickname, String slug);

    /**
     * 게시글 검색
     * - nickname = null  → Guest: PUBLISHED 상태만, 전체 사용자 대상
     * - nickname != null → User:  전체 상태(PUBLISHED+DRAFT), 본인 게시글만
     *
     * @param nickname 요청자 닉네임 (null = Guest, non-null = User)
     * @param keyword  검색 키워드
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시글 목록
     */
    Page<PostResponse.PostItem> searchPosts(String nickname, String keyword, Pageable pageable);

    /**
     * 내 게시글 검색 (전체 상태 포함)
     *
     * @param nickname 내 닉네임
     * @param keyword  검색 키워드
     * @param pageable 페이지네이션 정보
     * @return 내 게시글 목록
     */
    Page<PostMeResponse.MyPostItem> searchMyPosts(String nickname, String keyword, Pageable pageable);

    /**
     * 특정 사용자의 공개 게시글 검색
     * - PUBLISHED 상태만, 닉네임 일치하는 사용자의 게시글만
     *
     * @param nickname 검색 대상 사용자 닉네임
     * @param keyword  검색 키워드
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시글 목록
     */
    Page<PostResponse.PostItem> searchPostsByNickname(String nickname, String keyword, Pageable pageable);

    /**
     * 특정 사용자의 카테고리별 공개 게시글 목록 조회
     * - PUBLISHED 상태만, 닉네임 일치하는 사용자의 게시글만
     *
     * @param nickname     조회할 사용자 닉네임
     * @param categoryName 조회할 카테고리 이름 (Category.name, unique)
     * @param pageable     페이지네이션 정보
     * @return 해당 카테고리가 적용된 게시글 목록
     */
    Page<PostResponse.PostItem> getPostsByCategory(String nickname, String categoryName, Pageable pageable);

    // ========== Create, Update, Delete ========== //

    /**
     * 게시글 생성
     *
     * @param user 게시글 작성자
     * @param request 게시글 생성 요청 데이터
     * @return 생성된 게시글 상세 정보
     * @throws CustomException 제목 중복, 사용자 없음, 파일 없음 등
     */
    PostMeResponse.MyPostItem create(User user, PostMeRequest.Create request, MultipartFile thumbnail);

    /**
     * 게시글 수정용 데이터 조회
     *
     * @param userId 조회 요청 사용자 ID
     * @param slug 조회할 게시글 slug
     * @return 게시글 수정용 데이터
     * @throws CustomException 게시글을 찾을 수 없거나 권한이 없는 경우
     */
    PostMeResponse.MyPostEdit getEdit(Long userId, String slug);

    /**
     * 게시글 수정
     *
     * @param userId 수정 요청 사용자 ID
     * @param postId 수정할 게시글 ID
     * @param request 게시글 수정 요청 데이터
     * @return 수정된 게시글 상세 정보
     * @throws CustomException 게시글 없음, 권한 없음, 제목 중복 등
     */
    PostMeResponse.MyPostItem update(Long userId, Long postId, PostMeRequest.Update request, MultipartFile thumbnail);

    /**
     * 게시글 삭제 (소프트 삭제)
     *
     * @param userId 삭제 요청 사용자 ID
     * @param postId 삭제할 게시글 ID
     * @throws CustomException 게시글 없음, 권한 없음
     */
    void delete(Long userId, Long postId);

    /**
     * 게시글 복구
     * - DELETED 상태의 게시글을 PUBLISHED 상태로 복구
     *
     * @param userId 복구 요청 사용자 ID
     * @param postId 복구할 게시글 ID
     * @throws CustomException 게시글 없음, 권한 없음, 이미 복구된 게시글
     */
    void restore(Long userId, Long postId);

    /**
     * 이미지 업로드
     *
     * @param file   업로드할 파일
     * @param userId 요청 사용자 ID
     * @return 업로드된 파일 URL
     */
    String uploadImage(MultipartFile file, Long userId);

    /**
     * 삭제된 게시글 조회
     *
     * @param userId 조회 요청 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 삭제된 게시글 목록
     */
    Page<PostMeResponse.MyPostItem> getDeleted(Long userId, Pageable pageable);
}
