package com.sealog.backend.domain.feature.archive.service;

import com.sealog.backend.domain.feature.archive.dto.ArchiveRequest;
import com.sealog.backend.domain.feature.archive.dto.ArchiveResponse;
import com.sealog.backend.domain.feature.archive.entity.Archive;
import com.sealog.backend.domain.feature.archive.repository.ArchiveRepository;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.support.base.TestUnitBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ArchiveService 통합 테스트
 */
@Transactional
@DisplayName("ArchiveService 통합 테스트")
class ArchiveServiceTest extends TestUnitBase {

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    private UserRepository userRepository;

    // 수정되지 않는 엔티티 — @BeforeEach로 초기화
    private User owner;    // 아카이브 소유자 (ID=1)
    private User stranger; // 타인 — 권한 없음 (ID=2)

    @BeforeEach
    void setUp() {
        owner    = userRepository.findById(1L).orElseThrow();
        stranger = userRepository.findById(2L).orElseThrow();
    }


    // ===========================
    // add()
    // ===========================

    @Nested
    @DisplayName("아카이브 생성")
    class Add {

        @Test
        @DisplayName("아카이브 생성 - 성공")
        void 아카이브_생성_성공() {
            // given
            ArchiveRequest.Add request = ArchiveRequest.Add.builder()
                    .name("신규 아카이브")
                    .build();

            // when
            archiveService.add(owner.getId(), request);

            // then
            assertThat(archiveRepository.existsByNameAndUserId("신규 아카이브", owner.getId())).isTrue();
        }

        @Test
        @DisplayName("아카이브 생성 - 실패 - 존재하지 않는 사용자")
        void 아카이브_생성_실패_존재하지_않는_사용자() {
            // given
            ArchiveRequest.Add request = ArchiveRequest.Add.builder()
                    .name("신규 아카이브")
                    .build();

            // when & then
            assertThatThrownBy(() -> archiveService.add(999_999L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("아카이브 생성 - 실패 - 이름 중복")
        void 아카이브_생성_실패_이름_중복() {
            // given: 동일 이름 아카이브 사전 저장
            archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("중복 이름")
                    .slug("중복-이름")
                    .isPublic(true)
                    .build());

            ArchiveRequest.Add request = ArchiveRequest.Add.builder()
                    .name("중복 이름")
                    .build();

            // when & then
            assertThatThrownBy(() -> archiveService.add(owner.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }


    // ===========================
    // edit()
    // ===========================

    @Nested
    @DisplayName("아카이브 수정")
    class Edit {

        @Test
        @DisplayName("아카이브 수정 - 성공")
        void 아카이브_수정_성공() {
            // given
            Archive archive = archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("기존 이름")
                    .slug("기존-이름")
                    .isPublic(true)
                    .build());

            ArchiveRequest.Edit request = ArchiveRequest.Edit.builder()
                    .name("변경된 이름")
                    .build();

            // when
            archiveService.edit(owner.getId(), owner.getNickname(), "기존-이름", request);

            // then
            Archive updated = archiveRepository.findById(archive.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("변경된 이름");
            assertThat(updated.getSlug()).isNotEqualTo("기존-이름");
        }

        @Test
        @DisplayName("아카이브 수정 - 실패 - 아카이브 없음")
        void 아카이브_수정_실패_아카이브_없음() {
            // given
            ArchiveRequest.Edit request = ArchiveRequest.Edit.builder()
                    .name("변경된 이름")
                    .build();

            // when & then
            assertThatThrownBy(() -> archiveService.edit(owner.getId(), owner.getNickname(), "없는-slug", request))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("아카이브 수정 - 실패 - 소유자 아님")
        void 아카이브_수정_실패_소유자_아님() {
            // given
            archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("소유자 아카이브")
                    .slug("소유자-아카이브")
                    .isPublic(true)
                    .build());

            ArchiveRequest.Edit request = ArchiveRequest.Edit.builder()
                    .name("탈취된 이름")
                    .build();

            // when & then
            assertThatThrownBy(() -> archiveService.edit(stranger.getId(), owner.getNickname(), "소유자-아카이브", request))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("아카이브 수정 - 실패 - 동일 이름")
        void 아카이브_수정_실패_동일_이름() {
            // given
            archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("동일 이름")
                    .slug("동일-이름")
                    .isPublic(true)
                    .build());

            ArchiveRequest.Edit request = ArchiveRequest.Edit.builder()
                    .name("동일 이름")
                    .build();

            // when & then
            assertThatThrownBy(() -> archiveService.edit(owner.getId(), owner.getNickname(), "동일-이름", request))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }


    // ===========================
    // show()
    // ===========================

    @Nested
    @DisplayName("아카이브 공개")
    class Show {

        @Test
        @DisplayName("아카이브 공개 - 성공")
        void 아카이브_공개_성공() {
            // given: 비공개 상태로 저장
            Archive archive = archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("비공개 아카이브")
                    .slug("비공개-아카이브")
                    .isPublic(false)
                    .build());

            // when
            archiveService.show(owner.getId(), owner.getNickname(), "비공개-아카이브");

            // then
            Archive updated = archiveRepository.findById(archive.getId()).orElseThrow();
            assertThat(updated.isPublic()).isTrue();
        }

        @Test
        @DisplayName("아카이브 공개 - 실패 - 아카이브 없음")
        void 아카이브_공개_실패_아카이브_없음() {
            assertThatThrownBy(() -> archiveService.show(owner.getId(), owner.getNickname(), "없는-slug"))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("아카이브 공개 - 실패 - 소유자 아님")
        void 아카이브_공개_실패_소유자_아님() {
            // given
            archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("소유자 아카이브")
                    .slug("소유자-아카이브")
                    .isPublic(false)
                    .build());

            // when & then
            assertThatThrownBy(() -> archiveService.show(stranger.getId(), owner.getNickname(), "소유자-아카이브"))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }


    // ===========================
    // hide()
    // ===========================

    @Nested
    @DisplayName("아카이브 비공개")
    class Hide {

        @Test
        @DisplayName("아카이브 비공개 - 성공")
        void 아카이브_비공개_성공() {
            // given: 공개 상태로 저장
            Archive archive = archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("공개 아카이브")
                    .slug("공개-아카이브")
                    .isPublic(true)
                    .build());

            // when
            archiveService.hide(owner.getId(), owner.getNickname(), "공개-아카이브");

            // then
            Archive updated = archiveRepository.findById(archive.getId()).orElseThrow();
            assertThat(updated.isPublic()).isFalse();
        }

        @Test
        @DisplayName("아카이브 비공개 - 실패 - 아카이브 없음")
        void 아카이브_비공개_실패_아카이브_없음() {
            assertThatThrownBy(() -> archiveService.hide(owner.getId(), owner.getNickname(), "없는-slug"))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("아카이브 비공개 - 실패 - 소유자 아님")
        void 아카이브_비공개_실패_소유자_아님() {
            // given
            archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("소유자 아카이브")
                    .slug("소유자-아카이브")
                    .isPublic(true)
                    .build());

            // when & then
            assertThatThrownBy(() -> archiveService.hide(stranger.getId(), owner.getNickname(), "소유자-아카이브"))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }


    // ===========================
    // remove()
    // ===========================

    @Nested
    @DisplayName("아카이브 삭제")
    class Remove {

        @Test
        @DisplayName("아카이브 삭제 - 성공")
        void 아카이브_삭제_성공() {
            // given
            Archive archive = archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("삭제 대상")
                    .slug("삭제-대상")
                    .isPublic(true)
                    .build());

            // when
            archiveService.remove(owner.getId(), owner.getNickname(), "삭제-대상");

            // then
            assertThat(archiveRepository.findById(archive.getId())).isEmpty();
        }

        @Test
        @DisplayName("아카이브 삭제 - 실패 - 아카이브 없음")
        void 아카이브_삭제_실패_아카이브_없음() {
            assertThatThrownBy(() -> archiveService.remove(owner.getId(), owner.getNickname(), "없는-slug"))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("아카이브 삭제 - 실패 - 소유자 아님")
        void 아카이브_삭제_실패_소유자_아님() {
            // given
            archiveRepository.save(Archive.builder()
                    .user(owner)
                    .name("소유자 아카이브")
                    .slug("소유자-아카이브")
                    .isPublic(true)
                    .build());

            // when & then
            assertThatThrownBy(() -> archiveService.remove(stranger.getId(), owner.getNickname(), "소유자-아카이브"))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }


    // ===========================
    // getPagedPublicItemsByNickname()
    // ===========================

    @Nested
    @DisplayName("닉네임 기준 공개 아카이브 목록 조회")
    class GetPagedPublicItemsByNickname {

        @Test
        @DisplayName("아카이브 공개 목록 조회 - 성공 - 공개만 반환, 비공개·타인 제외")
        void 아카이브_공개_목록_조회_성공() {
            // given
            archiveRepository.save(Archive.builder().user(owner).name("공개1").slug("공개-1").isPublic(true).build());
            archiveRepository.save(Archive.builder().user(owner).name("공개2").slug("공개-2").isPublic(true).build());
            archiveRepository.save(Archive.builder().user(owner).name("비공개").slug("비공개-1").isPublic(false).build()); // 제외
            archiveRepository.save(Archive.builder().user(stranger).name("타인공개").slug("타인-공개").isPublic(true).build()); // 제외

            // when
            Page<ArchiveResponse.ArchiveItems> result =
                    archiveService.getPagedPublicItemsByNickname(owner.getNickname(), PageRequest.of(0, 10));

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting("name")
                    .containsExactlyInAnyOrder("공개1", "공개2");
        }

        @Test
        @DisplayName("아카이브 공개 목록 조회 - 성공 - 공개 아카이브 없으면 빈 페이지")
        void 아카이브_공개_목록_조회_성공_공개_없음() {
            // given: 비공개 아카이브만 존재
            archiveRepository.save(Archive.builder().user(owner).name("비공개").slug("비공개-1").isPublic(false).build());

            // when
            Page<ArchiveResponse.ArchiveItems> result =
                    archiveService.getPagedPublicItemsByNickname(owner.getNickname(), PageRequest.of(0, 10));

            // then
            assertThat(result.getTotalElements()).isZero();
        }
    }


    // ===========================
    // getPagedItemsForUser()
    // ===========================

    @Nested
    @DisplayName("내 아카이브 목록 조회")
    class GetPagedItemsForUser {

        @Test
        @DisplayName("아카이브 내 목록 조회 - 성공 - 공개·비공개 포함, 타인 제외")
        void 아카이브_내_목록_조회_성공() {
            // given
            archiveRepository.save(Archive.builder().user(owner).name("내 공개").slug("내-공개").isPublic(true).build());
            archiveRepository.save(Archive.builder().user(owner).name("내 비공개").slug("내-비공개").isPublic(false).build());
            archiveRepository.save(Archive.builder().user(stranger).name("타인").slug("타인").isPublic(true).build()); // 제외

            // when
            Page<ArchiveResponse.ArchiveItems> result =
                    archiveService.getPagedItemsForUser(owner.getId(), PageRequest.of(0, 10));

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting("name")
                    .containsExactlyInAnyOrder("내 공개", "내 비공개");
        }
    }
}
