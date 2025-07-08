package com.example.kidsreading.repository;

import com.example.kidsreading.entity.SentenceLearningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SentenceLearningSessionRepository extends JpaRepository<SentenceLearningSession, Long> {
    
    List<SentenceLearningSession> findByUserId(Long userId);
    
    List<SentenceLearningSession> findBySentenceId(Long sentenceId);
    
    List<SentenceLearningSession> findByUserIdAndSentenceId(Long userId, Long sentenceId);
    
    List<SentenceLearningSession> findByUserIdAndIsCompletedTrue(Long userId);
    
    @Query("SELECT sls FROM SentenceLearningSession sls WHERE sls.userId = :userId AND sls.sessionStartTime >= :startDate")
    List<SentenceLearningSession> findByUserIdAndDateAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT AVG(sls.completionTimeSeconds) FROM SentenceLearningSession sls WHERE sls.userId = :userId AND sls.isCompleted = true")
    Double getAverageCompletionTimeByUser(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(sls) FROM SentenceLearningSession sls WHERE sls.userId = :userId AND sls.isCompleted = true")
    Long getCompletedSessionsCountByUser(@Param("userId") Long userId);
    
    @Query("SELECT SUM(sls.coinsEarned) FROM SentenceLearningSession sls WHERE sls.userId = :userId")
    Integer getTotalCoinsEarnedByUser(@Param("userId") Long userId);
} 