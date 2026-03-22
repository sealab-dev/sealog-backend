package com.sealog.backend.domain.feature.post.service;

import com.sealog.backend.domain.feature.post.dto.PostResponse;
import com.sealog.backend.domain.feature.post.entity.Post;
import com.sealog.backend.domain.feature.post.entity.PostCategory;
import com.sealog.backend.domain.feature.post.repository.PostCategoryRepository;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.category.entity.Category;
import com.sealog.backend.domain.feature.category.repository.CategoryRepository;
import com.sealog.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCategoryServiceImpl implements PostCategoryService {

    private static final int MAX_CATEGORIES_PER_POST = 5;

    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void savePostCategories(Long postId, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            postCategoryRepository.deleteAllByPostId(postId);
            return;
        }

        if (categoryIds.size() > MAX_CATEGORIES_PER_POST) {
            throw CustomException.badRequest("카테고리는 최대 " + MAX_CATEGORIES_PER_POST + "개까지 등록할 수 있습니다");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> CustomException.notFound("게시글을 찾을 수 없습니다"));

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw CustomException.badRequest("존재하지 않는 카테고리가 포함되어 있습니다");
        }

        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        postCategoryRepository.deleteAllByPostId(postId);

        List<PostCategory> postCategories = new ArrayList<>();
        for (int i = 0; i < categoryIds.size(); i++) {
            postCategories.add(PostCategory.builder()
                    .post(post)
                    .category(categoryMap.get(categoryIds.get(i)))
                    .sortOrder(i)
                    .build());
        }

        postCategoryRepository.saveAll(postCategories);
    }

    @Override
    public List<PostCategory> getPostCategoriesByPostId(Long postId) {
        return postCategoryRepository.findAllByPostIdOrderBySortOrderAsc(postId);
    }

    @Override
    public List<PostResponse.CategoryItem> getCategoryItemsByPostId(Long postId) {
        return postCategoryRepository.findAllByPostIdOrderBySortOrderAsc(postId).stream()
                .map(pc -> PostResponse.CategoryItem.of(
                        pc.getCategory().getId(),
                        pc.getCategory().getName(),
                        pc.getSortOrder()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllByPostId(Long postId) {
        postCategoryRepository.deleteAllByPostId(postId);
    }
}
