package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

    Optional<UserStreak> findByUserId(Long userId);

    @Query("SELECT us FROM UserStreak us WHERE us.currentStreak > 0 ORDER BY us.currentStreak DESC")
    List<UserStreak> findTopStreaks();

    @Query("SELECT us FROM UserStreak us WHERE us.maxStreak > 0 ORDER BY us.maxStreak DESC")
    List<UserStreak> findTopMaxStreaks();

    @Query("SELECT COUNT(us) FROM UserStreak us WHERE us.currentStreak >= :minStreak")
    Long countUsersWithStreakAtLeast(@Param("minStreak") int minStreak);

    @Query("SELECT AVG(us.currentStreak) FROM UserStreak us WHERE us.currentStreak > 0")
    Double getAverageCurrentStreak();

    @Query("SELECT AVG(us.maxStreak) FROM UserStreak us WHERE us.maxStreak > 0")
    Double getAverageMaxStreak();

    @Query("SELECT us FROM UserStreak us WHERE us.lastLearningDate = :date")
    List<UserStreak> findByLastLearningDate(@Param("date") LocalDate date);

    @Query("SELECT us FROM UserStreak us WHERE us.lastLearningDate >= :startDate AND us.lastLearningDate <= :endDate")
    List<UserStreak> findByLastLearningDateBetween(@Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(us) FROM UserStreak us WHERE us.lastLearningDate = :date")
    Long countActiveUsersOnDate(@Param("date") LocalDate date);
} 