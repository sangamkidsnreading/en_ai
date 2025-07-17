package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserSentenceProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSentenceProgressRepository extends JpaRepository<UserSentenceProgress, Long> {

    // 특정 사용자 전체 진행 이력 조회
    List<UserSentenceProgress> findByUserId(Long userId);

    // 특정 사용자-문장 진행 이력 조회
    Optional<UserSentenceProgress> findByUserIdAndSentenceId(Long userId, Long sentenceId);

    // 특정 사용자-문장 진행 여부 체크
    boolean existsByUserIdAndSentenceId(Long userId, Long sentenceId);

    // 특정 사용자가 완료한 문장 목록 조회
    List<UserSentenceProgress> findByUserIdAndIsCompletedTrue(Long userId);

    // 특정 사용자의 전체 이력 수 조회
    long countByUserId(Long userId);

    // 주어진 기간 동안 생성된 이력 수 조회
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 특정 사용자가 특정 레벨·일차 문장을 완료한 개수 조회
     * 파생쿼리: user.id, sentence.difficultyLevel, sentence.dayNumber, isCompleted
     */
    int countByUserIdAndSentence_DifficultyLevelAndSentence_DayNumberAndIsCompletedTrue(
            Long userId,
            Integer difficultyLevel,
            Integer dayNumber
    );

    /**
     * 오늘 완료한 문장 수
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM UserSentenceProgress u WHERE u.userId = :userId AND u.isCompleted = true AND FUNCTION('DATE', u.lastLearnedAt) = CURRENT_DATE")
    int countTodayCompletedSentences(@org.springframework.data.repository.query.Param("userId") Long userId);

    /**
     * 전체 완료한 문장 수
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM UserSentenceProgress u WHERE u.userId = :userId AND u.isCompleted = true")
    int countTotalCompletedSentences(@org.springframework.data.repository.query.Param("userId") Long userId);

    /**
     * 완료된 문장 ID 리스트
     */
    @org.springframework.data.jpa.repository.Query("SELECT u.sentenceId FROM UserSentenceProgress u WHERE u.userId = :userId AND u.isCompleted = true")
    java.util.List<Long> findCompletedSentenceIdsByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    /**
     * 특정 날짜 범위에서 사용자가 완료한 문장 수 조회
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM UserSentenceProgress u WHERE u.userId = :userId AND u.isCompleted = true AND u.lastLearnedAt BETWEEN :startDate AND :endDate")
    int countCompletedSentencesByUserAndDateRange(@org.springframework.data.repository.query.Param("userId") Long userId,
                                                  @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate,
                                                  @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate);

    int countByUserIdAndIsLearnedTrue(Long userId);
}