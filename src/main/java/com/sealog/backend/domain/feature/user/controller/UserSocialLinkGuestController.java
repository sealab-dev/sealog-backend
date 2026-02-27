package com.sealog.backend.domain.feature.user.controller;

import com.sealog.backend.domain.feature.user.dto.UserSocialLinkResponse;
import com.sealog.backend.domain.feature.user.service.UserSocialLinkService;
import com.sealog.backend.global.response.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest/user")
@RequiredArgsConstructor
public class UserSocialLinkGuestController implements UserSocialLinkGuestControllerDocs {

    private final UserSocialLinkService userSocialLinkService;

    /**
     * 특정 사용자의 소셜 링크 조회
     * GET /api/guest/user/{nickname}/social-links
     * 게스트
     */
    @Override
    @GetMapping("/{nickname}/social-links")
    public ResponseEntity<CustomResponse<List<UserSocialLinkResponse.LinkInfo>>> getPublicLinks(
            @PathVariable String nickname
    ) {
        List<UserSocialLinkResponse.LinkInfo> response = userSocialLinkService.getPublicLinks(nickname);
        return ResponseEntity.ok(CustomResponse.success(response));
    }
}
