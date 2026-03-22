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
 */
public interface PostService {

    // ========== Read (Public) ========== //

    /**
     * 전체 공개 게시글 목록 조회
     */
    Page<PostResponse.PostItem> getPosts(Pageable pageable);

    /**
     * 특정 사용자의 공개 게시글 목록 조회
     */
    Page<PostResponse.PostItem> getUserPosts(String nickname, Pageable pageable);

    /**
     * 전체 공개 게시글 검색
     * @param keyword 검색 키워드
     * @param pageable 페이지네이션
     */
    Page<PostResponse.PostItem> searchPosts(String keyword, Pageable pageable);

    /**
     * 특정 사용자의 공개 게시글 검색
     * @param nickname 대상 사용자 닉네임
     * @param keyword 검색 키워드
     * @param pageable 페이지네이션
     */
    Page<PostResponse.PostItem> searchPostsByNickname(String nickname, String keyword, Pageable pageable);

    /**
     * 특정 사용자의 카테고리별 공개 게시글 목록 조회
     */
    Page<PostResponse.PostItem> getPostsByCategory(String nickname, String categoryName, Pageable pageable);

    /**
     * 게시글 상세 조회
     */
    PostResponse.PostDetail getDetail(String nickname, String slug);

    // ========== Read (Authenticated) ========== //

    /**
     * 내 게시글 검색 (DRAFT 포함)
     */
    Page<PostMeResponse.MyPostItem> searchMyPosts(String nickname, String keyword, Pageable pageable);

    /**
     * 삭제된 내 게시글 조회 (휴지통)
     */
    Page<PostMeResponse.MyPostItem> getDeleted(Long userId, Pageable pageable);

    /**
     * 게시글 수정용 데이터 조회
     */
    PostMeResponse.MyPostEdit getEdit(Long userId, String slug);

    // ========== Create, Update, Delete ========== //

    /**
     * 게시글 생성
     */
    PostMeResponse.MyPostItem create(User user, PostMeRequest.Create request, MultipartFile thumbnail);

    /**
     * 게시글 수정
     */
    PostMeResponse.MyPostItem update(Long userId, Long postId, PostMeRequest.Update request, MultipartFile thumbnail);

    /**
     * 게시글 삭제 (소프트 삭제)
     */
    void delete(Long userId, Long postId);

    /**
     * 게시글 복구
     */
    void restore(Long userId, Long postId);

    /**
     * 이미지 업로드
     */
    String uploadImage(MultipartFile file, Long userId);
}
