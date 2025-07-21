package com.example.kidsreading.repository;

import com.example.kidsreading.entity.UserLevels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLevelsRepository extends JpaRepository<UserLevels, Long> {
    
    /**
     * 사용자 ID로 UserLevels 조회
     */
    Optional<UserLevels> findByUserId(Long userId);
    
    /**
     * 사용자 ID로 현재 레벨 조회
     */
    @Query("SELECT ul.currentLevel FROM UserLevels ul WHERE ul.userId = :userId")
    Optional<Integer> findCurrentLevelByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자 ID로 경험치 조회
     */
    @Query("SELECT ul.experiencePoints FROM UserLevels ul WHERE ul.userId = :userId")
    Optional<Integer> findExperiencePointsByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자 ID로 총 코인 조회
     */
    @Query("SELECT ul.totalCoins FROM UserLevels ul WHERE ul.userId = :userId")
    Optional<Integer> findTotalCoinsByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자 ID로 연속 학습일 조회
     */
    @Query("SELECT ul.streakDays FROM UserLevels ul WHERE ul.userId = :userId")
    Optional<Integer> findStreakDaysByUserId(@Param("userId") Long userId);
} 