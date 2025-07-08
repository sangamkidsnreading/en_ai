package com.example.kidsreading.repository;

import com.example.kidsreading.entity.SentenceStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SentenceStatisticsRepository extends JpaRepository<SentenceStatistics, Long> {
    
    Optional<SentenceStatistics> findBySentenceId(Long sentenceId);
    
    List<SentenceStatistics> findBySentenceIdIn(List<Long> sentenceIds);
    
    @Query("SELECT ss FROM SentenceStatistics ss WHERE ss.difficultyRating >= :minRating ORDER BY ss.popularityScore DESC")
    List<SentenceStatistics> findPopularSentences(@Param("minRating") Double minRating);
    
    @Query("SELECT ss FROM SentenceStatistics ss WHERE ss.averageCompletionTime <= :maxTime ORDER BY ss.successfulAttempts DESC")
    List<SentenceStatistics> findEasySentences(@Param("maxTime") Double maxTime);
    
    @Query("SELECT AVG(ss.difficultyRating) FROM SentenceStatistics ss WHERE ss.sentenceId IN :sentenceIds")
    Double getAverageDifficultyRating(@Param("sentenceIds") List<Long> sentenceIds);
} 