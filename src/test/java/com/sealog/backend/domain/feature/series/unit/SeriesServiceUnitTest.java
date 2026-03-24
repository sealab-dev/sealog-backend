package com.sealog.backend.domain.feature.series.unit;

import com.sealog.backend.domain.feature.series.dto.SeriesMeRequest;
import com.sealog.backend.domain.feature.series.entity.Series;
import com.sealog.backend.domain.feature.series.repository.SeriesRepository;
import com.sealog.backend.domain.feature.series.service.SeriesServiceImpl;
import com.sealog.backend.domain.feature.post.repository.PostRepository;
import com.sealog.backend.domain.feature.post.service.PostCategoryService;
import com.sealog.backend.domain.feature.post.service.PostTagService;
import com.sealog.backend.domain.feature.user.entity.User;
import com.sealog.backend.domain.feature.user.repository.UserRepository;
import com.sealog.backend.global.exception.CustomException;
import com.sealog.backend.infra.storage.service.FileStorageService;
import com.sealog.backend.support.base.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * SeriesService 단위 테스트
 */
@DisplayName("SeriesService 단위 테스트")
class SeriesServiceUnitTest extends UnitTest {

    @InjectMocks SeriesServiceImpl seriesService;

    @Mock SeriesRepository seriesRepository;
    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;
    @Mock PostTagService postTagService;
    @Mock PostCategoryService postCategoryService;
    @Mock FileStorageService fileStorageService;

    // =====================================================================
    // 시리즈 생성
    // =====================================================================
    @Nested
    @DisplayName("시리즈 생성 (create)")
    class 시리즈_생성 {

        @Test
        @DisplayName("이름 중복 → 400")
        void 이름_중복() {
            // given
            Long userId = 1L;
            User mockUser = mock(User.class);
            SeriesMeRequest.Create request = SeriesMeRequest.Create.builder()
                    .name("기존 시리즈")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(seriesRepository.existsByNameAndUserId("기존 시리즈", userId)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> seriesService.create(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // =====================================================================
    // 시리즈 수정
    // =====================================================================
    @Nested
    @DisplayName("시리즈 수정 (update)")
    class 시리즈_수정 {

        @Test
        @DisplayName("시리즈 없음 → 404")
        void 시리즈_없음() {
            // given
            when(seriesRepository.findById(99L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> seriesService.update(1L, 99L, SeriesMeRequest.Update.builder().name("이름").build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("권한 없음 → 403")
        void 권한_없음() {
            // given
            Long ownerId = 1L;
            Long otherUserId = 2L;

            User mockOwner = mock(User.class);
            when(mockOwner.getId()).thenReturn(ownerId);

            Series mockSeries = mock(Series.class);
            when(mockSeries.getUser()).thenReturn(mockOwner);
            when(mockSeries.getName()).thenReturn("기존 이름");

            when(seriesRepository.findById(10L)).thenReturn(Optional.of(mockSeries));

            // when & then
            assertThatThrownBy(() -> seriesService.update(otherUserId, 10L, SeriesMeRequest.Update.builder().name("새 이름").build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("동일한 이름으로 수정 → 400")
        void 동일한_이름으로_수정() {
            // given
            Long userId = 1L;

            User mockOwner = mock(User.class);
            when(mockOwner.getId()).thenReturn(userId);

            Series mockSeries = mock(Series.class);
            when(mockSeries.getUser()).thenReturn(mockOwner);
            when(mockSeries.getName()).thenReturn("기존 이름");

            when(seriesRepository.findById(10L)).thenReturn(Optional.of(mockSeries));

            // when & then
            assertThatThrownBy(() -> seriesService.update(userId, 10L, SeriesMeRequest.Update.builder().name("기존 이름").build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("이미 존재하는 이름으로 수정 → 409")
        void 이미_존재하는_이름으로_수정() {
            // given
            Long userId = 1L;

            User mockOwner = mock(User.class);
            when(mockOwner.getId()).thenReturn(userId);

            Series mockSeries = mock(Series.class);
            when(mockSeries.getUser()).thenReturn(mockOwner);
            when(mockSeries.getName()).thenReturn("기존 이름");

            when(seriesRepository.findById(10L)).thenReturn(Optional.of(mockSeries));
            when(seriesRepository.existsByNameAndUserId("중복 이름", userId)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> seriesService.update(userId, 10L, SeriesMeRequest.Update.builder().name("중복 이름").build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.CONFLICT);
        }
    }

    // =====================================================================
    // 시리즈 조회 (ID + 소유권 검증)
    // =====================================================================
    @Nested
    @DisplayName("시리즈 조회 (getByIdAndUserId)")
    class 시리즈_조회 {

        @Test
        @DisplayName("시리즈 없음 → 404")
        void 시리즈_없음() {
            // given
            when(seriesRepository.findById(99L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> seriesService.getByIdAndUserId(99L, 1L))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("권한 없음 → 403")
        void 권한_없음() {
            // given
            Long ownerId = 1L;
            Long otherUserId = 2L;

            User mockOwner = mock(User.class);
            when(mockOwner.getId()).thenReturn(ownerId);

            Series mockSeries = mock(Series.class);
            when(mockSeries.getUser()).thenReturn(mockOwner);

            when(seriesRepository.findById(10L)).thenReturn(Optional.of(mockSeries));

            // when & then
            assertThatThrownBy(() -> seriesService.getByIdAndUserId(10L, otherUserId))
                    .isInstanceOf(CustomException.class)
                    .extracting("status")
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }
}
