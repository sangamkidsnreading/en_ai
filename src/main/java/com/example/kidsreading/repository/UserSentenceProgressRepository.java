
package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserSentenceProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSentenceProgressRepository extends JpaRepository<UserSentenceProgress, Long> {

    Optional<UserSentenceProgress> findByUserIdAndSentenceId(String userId, Long sentenceId);

    List<UserSentenceProgress> findByUserId(String userId);

    @Query("SELECT COUNT(usp) FROM UserSentenceProgress usp " +
           "JOIN usp.sentence s " +
           "WHERE usp.userId = :userId " +
           "AND s.difficultyLevel = :level " +
           "AND s.dayNumber = :day " +
           "AND usp.isCompleted = true")
    long countByUserIdStringAndSentence_DifficultyLevelAndSentence_DayNumberAndIsCompletedTrue(
            @Param("userId") String userId,
            @Param("level") Integer level,
            @Param("day") Integer day);

    @Query("SELECT usp FROM UserSentenceProgress usp " +
           "JOIN usp.sentence s " +
           "WHERE usp.userId = :userId " +
           "AND s.difficultyLevel = :level " +
           "AND s.dayNumber = :day")
    List<UserSentenceProgress> findByUserIdAndSentenceLevelAndDay(
            @Param("userId") String userId,
            @Param("level") Integer level,
            @Param("day") Integer day);
}
