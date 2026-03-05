package com.sealog.backend.domain.feature.stack.repository;

import com.sealog.backend.domain.feature.stack.entity.Stack;
import com.sealog.backend.domain.feature.stack.enums.StackGroup;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StackRepository extends JpaRepository<Stack, Long> {

    /**
     * 스택명으로 스택 조회
     *
     * @param name 스택명
     * @return 스택 (Optional)
     */
    Optional<Stack> findByName(String name);

    /**
     * 그룹별 스택 목록 조회
     *
     * @param stackGroup 스택 그룹
     * @return 해당 그룹의 스택 목록
     */
    List<Stack> findByStackGroup(StackGroup stackGroup);

    /**
     * 스택명 존재 여부 확인
     *
     * @param name 스택명
     * @return 존재 여부
     */
    boolean existsByName(String name);

    /**
     * 스택명 부분 일치 검색
     */
    List<Stack> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword, Pageable pageable);
}