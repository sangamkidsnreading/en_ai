package com.example.kidsreading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceDto {
    private Long id;
    private String text;
    private String sentence;
    private String translation;
    private String pronunciation;
    private Integer level;
    private Integer dayNumber;
    private String audioUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String korean;
    private String english;
    private String meaning;
    private String phonetic;
    private String sentenceText;
    private String sentenceTranslation;
    private Integer learnCount;
    private Integer correctCount;
    private Integer incorrectCount;
    private LocalDateTime lastStudied;
    private Boolean isCompleted;
    private Boolean hasRecording;
    private String recordingUrl;
    private LocalDateTime firstLearnedAt;
    private LocalDateTime lastLearnedAt;
    private Boolean isFavorite;
    private Boolean isLearned;
    private Integer totalDailyCoins;
    private Integer coinsEarned;
    private Integer dailyCoins;
    private Integer totalCoins;
    private Integer streakBonus;
    private Integer sentenceCoins;
    private Integer wordCoins;
    private Integer dailyGoal;
    private Integer levelUpCoin;
    private Integer maxLevel;
    private Integer dailyWordGoal;
    private Integer dailySentenceGoal;
    private Boolean voiceEnabled;
    private Integer day;
    private String voiceLanguage;
    private Boolean soundEffects;
    private Boolean autoPlay;
}