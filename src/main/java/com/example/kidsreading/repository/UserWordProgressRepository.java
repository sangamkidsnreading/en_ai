package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    Optional<UserWordProgress> findByUserIdAndWordId(String userId, Long wordId);

    List<UserWordProgress> findByUserId(String userId);

    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp " +
           "JOIN uwp.word w " +
           "WHERE uwp.userId = :userId " +
           "AND w.level = :level " +
           "AND w.day = :day " +
           "AND uwp.isCompleted = true")
    long countByUserIdAndWordLevelAndWordDayAndIsCompletedTrue(
            @Param("userId") String userId,
            @Param("level") Integer level,
            @Param("day") Integer day);

    @Query("SELECT uwp FROM UserWordProgress uwp " +
           "JOIN uwp.word w " +
           "WHERE uwp.userId = :userId " +
           "AND w.level = :level " +
           "AND w.day = :day")
    List<UserWordProgress> findByUserIdAndWordLevelAndDay(
            @Param("userId") String userId,
            @Param("level") Integer level,
            @Param("day") Integer day);

    long countDistinctUsersByLastStudiedAtAfter(LocalDateTime weekAgo);

    boolean existsByUserIdAndWordId(String userId, Long wordId);
    List<UserWordProgress> findByUserIdAndIsCompletedTrue(String userId);
    long countByUserId(String userId);
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}