package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    /**
     * 사용자와 단어 ID로 진행상황 조회
     */
    Optional<UserWordProgress> findByUserIdAndWordId(Long userId, Long wordId);

    /**
     * 사용자의 특정 레벨과 날짜에서 완료된 단어 수 조회
     */
    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp " +
            "JOIN Word w ON uwp.wordId = w.id " +
            "WHERE uwp.userId = :userId " +
            "AND w.level = :level " +
            "AND w.day = :day " +
            "AND uwp.isCompleted = true " +
            "AND w.isActive = true")
    int countCompletedWordsByUserAndLevelAndDay(@Param("userId") Long userId,
                                                @Param("level") Integer level,
                                                @Param("day") Integer day);

    /**
     * 사용자의 즐겨찾기 단어 목록 조회
     */
    @Query("SELECT uwp FROM UserWordProgress uwp " +
            "JOIN Word w ON uwp.wordId = w.id " +
            "WHERE uwp.userId = :userId " +
            "AND uwp.isFavorite = true " +
            "AND w.isActive = true " +
            "ORDER BY uwp.updatedAt DESC")
    List<UserWordProgress> findFavoriteWordsByUser(@Param("userId") Long userId);

    /**
     * 사용자의 완료된 단어 목록 조회
     */
    @Query("SELECT uwp FROM UserWordProgress uwp " +
            "JOIN Word w ON uwp.wordId = w.id " +
            "WHERE uwp.userId = :userId " +
            "AND uwp.isCompleted = true " +
            "AND w.isActive = true " +
            "ORDER BY uwp.updatedAt DESC")
    List<UserWordProgress> findCompletedWordsByUser(@Param("userId") Long userId);

    /**
     * 사용자의 특정 레벨 진행상황 조회
     */
    @Query("SELECT uwp FROM UserWordProgress uwp " +
            "JOIN Word w ON uwp.wordId = w.id " +
            "WHERE uwp.userId = :userId " +
            "AND w.level = :level " +
            "AND w.isActive = true " +
            "ORDER BY w.day ASC, w.id ASC")
    List<UserWordProgress> findByUserIdAndLevel(@Param("userId") Long userId,
                                                @Param("level") Integer level);

    long countDistinctUsersByLastStudiedAtAfter(LocalDateTime weekAgo);

    List<UserWordProgress> findByUserId(Long userId);
    boolean existsByUserIdAndWordId(Long userId, Long wordId);
    List<UserWordProgress> findByUserIdAndIsCompletedTrue(Long userId);
    long countByUserId(Long userId);
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    int countByUserIdAndIsLearnedTrue(Long userId);

    /**
     * 오늘 완료한 단어 수
     */
    @Query("SELECT COUNT(u) FROM UserWordProgress u WHERE u.userId = :userId AND u.isCompleted = true AND FUNCTION('DATE', u.lastLearnedAt) = CURRENT_DATE")
    int countTodayCompletedWords(@Param("userId") Long userId);

    /**
     * 전체 완료한 단어 수
     */
    @Query("SELECT COUNT(u) FROM UserWordProgress u WHERE u.userId = :userId AND u.isCompleted = true")
    int countTotalCompletedWords(@Param("userId") Long userId);

    /**
     * 완료된 단어 ID 리스트
     */
    @Query("SELECT u.wordId FROM UserWordProgress u WHERE u.userId = :userId AND u.isCompleted = true")
    List<Long> findCompletedWordIdsByUserId(@Param("userId") Long userId);

    /**
     * 특정 날짜 범위에서 사용자가 완료한 단어 수 조회
     */
    @Query("SELECT COUNT(u) FROM UserWordProgress u WHERE u.userId = :userId AND u.isCompleted = true AND u.lastLearnedAt BETWEEN :startDate AND :endDate")
    int countCompletedWordsByUserAndDateRange(@Param("userId") Long userId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 특정 기간 학습 완료된 단어 수 조회
     */
    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp " +
            "WHERE uwp.userId = :userId " +
            "AND uwp.isLearned = true " +
            "AND uwp.firstLearnedAt BETWEEN :startDate AND :endDate")
    int countByUserIdAndIsLearnedTrueAndFirstLearnedAtBetween(@Param("userId") Long userId,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 학습한 단어 수 조회 (최적화)
     */
    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp WHERE uwp.user.id = :userId AND uwp.isLearned = true")
    long countLearnedWordsByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 특정 날짜 학습한 단어 수 조회 (최적화)
     */
    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp WHERE uwp.user.id = :userId AND uwp.isLearned = true AND DATE(uwp.createdAt) = :date")
    long countLearnedWordsByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 사용자의 특정 날짜까지 누적 학습한 단어 수 조회 (최적화)
     */
    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp WHERE uwp.user.id = :userId AND uwp.isLearned = true AND DATE(uwp.createdAt) <= :date")
    long countLearnedWordsByUserIdUntilDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 사용자의 특정 기간 학습한 단어 수 조회 (최적화)
     */
    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp WHERE uwp.user.id = :userId AND uwp.isLearned = true AND DATE(uwp.createdAt) BETWEEN :startDate AND :endDate")
    long countLearnedWordsByUserIdBetweenDates(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Day/Level별 학습 진행도 조회
    @Query("SELECT w.day, w.level, COUNT(uwp) " +
           "FROM UserWordProgress uwp " +
           "JOIN uwp.word w " +
           "WHERE uwp.user.id = :userId " +
           "GROUP BY w.day, w.level " +
           "ORDER BY w.day, w.level")
    List<Object[]> findDayLevelProgressByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 특정 기간 이후 학습 완료된 단어 수 조회
     */
    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp " +
            "WHERE uwp.userId = :userId " +
            "AND uwp.isLearned = true " +
            "AND (uwp.firstLearnedAt >= :startDate OR (uwp.firstLearnedAt IS NULL AND uwp.createdAt >= :startDate))")
    long countByUserIdAndIsLearnedTrueAndFirstLearnedAtAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    /**
     * 사용자의 단어 복습 횟수 합계 조회 (learn_count)
     */
    @Query("SELECT COALESCE(SUM(uwp.learnCount), 0) FROM UserWordProgress uwp WHERE uwp.userId = :userId")
    long sumLearnCountByUserId(@Param("userId") Long userId);
}