package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserSentenceProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

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
    
    /**
     * 사용자가 완료한 문장 수 조회
     */
    long countByUserIdAndIsCompletedTrue(Long userId);
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

    /**
     * 사용자의 특정 기간 동안 학습 완료된 문장 수 조회
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(usp) FROM UserSentenceProgress usp " +
            "WHERE usp.userId = :userId " +
            "AND usp.isLearned = true " +
            "AND usp.firstLearnedAt BETWEEN :startDate AND :endDate")
    int countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(@org.springframework.data.repository.query.Param("userId") Long userId,
                                                               @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate,
                                                               @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 완료한 문장 수 조회 (최적화)
     */
    @Query("SELECT COUNT(usp) FROM UserSentenceProgress usp WHERE usp.user.id = :userId AND usp.isCompleted = true")
    long countCompletedSentencesByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 특정 날짜 완료한 문장 수 조회 (최적화)
     */
    @Query("SELECT COUNT(usp) FROM UserSentenceProgress usp WHERE usp.user.id = :userId AND usp.isCompleted = true AND DATE(usp.createdAt) = :date")
    long countCompletedSentencesByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 사용자의 특정 날짜까지 누적 완료한 문장 수 조회 (최적화)
     */
    @Query("SELECT COUNT(usp) FROM UserSentenceProgress usp WHERE usp.user.id = :userId AND usp.isCompleted = true AND DATE(usp.createdAt) <= :date")
    long countCompletedSentencesByUserIdUntilDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 사용자의 특정 기간 완료한 문장 수 조회 (최적화)
     */
    @Query("SELECT COUNT(usp) FROM UserSentenceProgress usp WHERE usp.user.id = :userId AND usp.isCompleted = true AND DATE(usp.createdAt) BETWEEN :startDate AND :endDate")
    long countCompletedSentencesByUserIdBetweenDates(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Day/Level별 학습 진행도 조회
    @Query("SELECT s.dayNumber, s.difficultyLevel, COUNT(usp) " +
           "FROM UserSentenceProgress usp " +
           "JOIN usp.sentence s " +
           "WHERE usp.user.id = :userId " +
           "GROUP BY s.dayNumber, s.difficultyLevel " +
           "ORDER BY s.dayNumber, s.difficultyLevel")
    List<Object[]> findDayLevelProgressByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 특정 기간 이후 학습 완료된 문장 수 조회
     */
    @Query("SELECT COUNT(usp) FROM UserSentenceProgress usp " +
            "WHERE usp.userId = :userId " +
            "AND usp.isLearned = true " +
            "AND (usp.firstLearnedAt >= :startDate OR (usp.firstLearnedAt IS NULL AND usp.createdAt >= :startDate))")
    long countByUserIdAndIsLearnedTrueAndFirstLearnedAtAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    /**
     * 사용자의 문장 복습 횟수 합계 조회 (learn_count)
     */
    @Query("SELECT COALESCE(SUM(usp.learnCount), 0) FROM UserSentenceProgress usp WHERE usp.userId = :userId")
    long sumLearnCountByUserId(@Param("userId") Long userId);
}