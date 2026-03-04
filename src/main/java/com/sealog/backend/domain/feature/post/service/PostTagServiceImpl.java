package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.entity.PostTag;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.post.repository.PostTagRepository;
import com.sealog.backend.domain.feature.tag.entity.Tag;
import com.sealog.backend.domain.feature.tag.repository.TagRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PostTag 비즈니스 로직 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostTagServiceImpl implements PostTagService {

    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final TagRepository tagRepository;

    /**
     * 게시글 태그 전체 교체
     * - 태그명 기준 findOrCreate 처리
     */
    @Override
    @Transactional
    public void updatePostTags(Long postId, List<String> tagNames) {
        postTagRepository.deleteAllByPostId(postId);

        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        List<PostTag> postTags = new ArrayList<>();
        for (int i = 0; i < tagNames.size(); i++) {
            Tag tag = findOrCreate(tagNames.get(i).trim());
            postTags.add(PostTag.builder()
                    .post(post)
                    .tag(tag)
                    .sortOrder(i)
                    .build());
        }

        postTagRepository.saveAll(postTags);
    }

    /**
     * 게시글에 연결된 태그 이름 목록 조회
     */
    @Override
    public List<String> getTagNamesByPostId(Long postId) {
        return postTagRepository.findAllByPostIdOrderBySortOrderAsc(postId).stream()
                .map(pt -> pt.getTag().getName())
                .collect(Collectors.toList());
    }

    /**
     * 게시글에 연결된 태그 매핑 전체 삭제 (스케줄러용)
     */
    @Override
    @Transactional
    public void deleteAllByPostId(Long postId) {
        postTagRepository.deleteAllByPostId(postId);
    }

    // ========== Private Methods ========== //

    /**
     * 태그명으로 태그 조회 또는 생성
     */
    private Tag findOrCreate(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
    }
}
