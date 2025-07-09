package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserSentenceProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSentenceProgressRepository extends JpaRepository<UserSentenceProgress, Long> {

    // 특정 사용자 전체 진행 이력 조회
    List<UserSentenceProgress> findByUserId(Long userId);
    boolean existsByUserIdAndSentenceId(Long userId, Long sentenceId);
    List<UserSentenceProgress> findByUserIdAndIsCompletedTrue(Long userId);

    /**
     * 사용자의 특정 레벨/Day 완료된 문장 수 조회
     */
    @Query("SELECT COUNT(usp) FROM UserSentenceProgress usp " +
           "LEFT JOIN Sentence s ON s.id = usp.sentenceId " +
           "WHERE usp.userId = :userId " +
           "AND s.difficultyLevel = :level " +
           "AND s.dayNumber = :day " +
           "AND usp.isCompleted = true")
    long countCompletedSentencesByUserAndLevelAndDay(@Param("userId") Long userId,
                                                    @Param("level") Integer level,
                                                    @Param("day") Integer day);

    /**
     * 사용자의 특정 레벨/Day 학습한 문장 수 조회 (완료되지 않았지만 학습은 한 것)
     */
    @Query("SELECT COUNT(usp) FROM UserSentenceProgress usp " +
           "LEFT JOIN Sentence s ON s.id = usp.sentenceId " +
           "WHERE usp.userId = :userId " +
           "AND s.difficultyLevel = :level " +
           "AND s.dayNumber = :day " +
           "AND usp.learnCount > 0")
    long countStudiedSentencesByUserAndLevelAndDay(@Param("userId") Long userId,
                                                  @Param("level") Integer level,
                                                  @Param("day") Integer day);

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
    long countByUserIdAndIsLearned(Long userId, Boolean isLearned);

    Optional<UserSentenceProgress> findByUserIdAndSentenceId(Long userId, Long sentenceId);
    long countByLastStudiedAtBetween(LocalDateTime start, LocalDateTime end);
}