package com.example.kidsreading.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    // 기본 통계
    private Integer wordsLearned;
    private Integer sentencesLearned;
    private Integer totalCoins;
    private Integer streakDays;
    
    // 레벨 정보
    private Integer currentLevel;
    private Integer levelProgress;
    private Integer wordsToNextLevel;
    
    // 뱃지 정보
    private List<BadgeDto> badges;
    private Integer totalBadges;
    private Integer earnedBadges;
    
    // 달력 정보
    private Map<String, CalendarDayDto> calendarData;
    private Integer currentMonth;
    private Integer currentYear;
    
    // 랭킹 정보
    private List<RankingDto> rankings;
    private Integer userRank;
    
    // 학습 진행도
    private Double completionRate;
    private Integer totalWords;
    private Integer totalSentences;
    
    // 일일 목표
    private Integer dailyWordGoal;
    private Integer dailySentenceGoal;
    private Integer dailyWordProgress;
    private Integer dailySentenceProgress;
    
    // 이전 데이터 비교용
    private Integer previousWordsLearned;
    private Integer previousSentencesLearned;
    private Integer previousTotalCoins;
}

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BadgeDto {
    private String id;
    private String name;
    private String icon;
    private String description;
    private Boolean isEarned;
    private LocalDate earnedDate;
}

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CalendarDayDto {
    private LocalDate date;
    private String status; // "completed", "partial", "not-started", "current"
    private Integer wordsCompleted;
    private Integer sentencesCompleted;
    private Integer coinsEarned;
}