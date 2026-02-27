package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserSocialLinkRequest;
import com.sealog.backend.domain.feature.user.dto.UserSocialLinkResponse;
import com.sealog.backend.domain.feature.user.service.UserSocialLinkService;
import com.sealog.backend.global.response.CustomResponse;
import com.sealog.backend.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/me/social-links")
@RequiredArgsConstructor
public class UserSocialLinkMeController implements UserSocialLinkMeControllerDocs {

    private final UserSocialLinkService userSocialLinkService;

    /**
     * 내 소셜 링크 목록 조회
     * GET /api/user/me/social-links
     * user
     */
    @Override
    @GetMapping
    public ResponseEntity<CustomResponse<List<UserSocialLinkResponse.LinkInfo>>> getMyLinks(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserSocialLinkResponse.LinkInfo> response = userSocialLinkService.getMyLinks(userDetails.getUserId());
        return ResponseEntity.ok(CustomResponse.success(response));
    }

    /**
     * 소셜 링크 전체 upsert
     * PUT /api/user/me/social-links
     * user
     */
    @Override
    @PutMapping
    public ResponseEntity<CustomResponse<List<UserSocialLinkResponse.LinkInfo>>> upsertLinks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserSocialLinkRequest.UpsertRequest request
    ) {
        List<UserSocialLinkResponse.LinkInfo> response = userSocialLinkService.upsertLinks(
                userDetails.getUserId(),
                request
        );
        return ResponseEntity.ok(CustomResponse.success(response, "소셜 링크가 저장되었습니다"));
    }
}
