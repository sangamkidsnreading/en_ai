package com.example.kidsreading.repository;

import com.example.kidsreading.entity.BadgeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeSettingsRepository extends JpaRepository<BadgeSettings, Long> {
    
    // 활성화된 뱃지 설정 조회 (정렬 순서대로)
    List<BadgeSettings> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    // 출석 뱃지 설정 조회
    List<BadgeSettings> findByAttendanceCountIsNotNullAndIsActiveTrueOrderByAttendanceCountAsc();
    
    // 연속출석 뱃지 설정 조회
    List<BadgeSettings> findByStreakCountIsNotNullAndIsActiveTrueOrderByStreakCountAsc();
    
    // 단어 뱃지 설정 조회
    List<BadgeSettings> findByWordsCountIsNotNullAndIsActiveTrueOrderByWordsCountAsc();
    
    // 문장 뱃지 설정 조회
    List<BadgeSettings> findBySentencesCountIsNotNullAndIsActiveTrueOrderBySentencesCountAsc();
    
    // 복습단어 뱃지 설정 조회
    List<BadgeSettings> findByWordReviewCountIsNotNullAndIsActiveTrueOrderByWordReviewCountAsc();
    
    // 복습문장 뱃지 설정 조회
    List<BadgeSettings> findBySentenceReviewCountIsNotNullAndIsActiveTrueOrderBySentenceReviewCountAsc();
} 